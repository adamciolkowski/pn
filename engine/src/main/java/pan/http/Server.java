package pan.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import pan.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Server {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        Path uiDir = findUiDir();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/evaluate", new ApiHandler());
        if (uiDir != null) {
            server.createContext("/", new StaticHandler(uiDir));
            System.out.println("Serving UI from " + uiDir.toAbsolutePath());
        } else {
            System.out.println("UI directory not found — API only on /api/evaluate");
        }
        server.start();
        System.out.println("Server started on http://localhost:" + server.getAddress().getPort());
    }

    private static Path findUiDir() {
        Path cwd = Path.of("").toAbsolutePath().normalize();
        for (Path candidate : List.of(
                cwd.resolve("ui"),
                cwd.resolve("../ui"),
                cwd.getParent() != null ? cwd.getParent().resolve("ui") : cwd.resolve("ui")
        )) {
            if (Files.isDirectory(candidate) && Files.isRegularFile(candidate.resolve("index.html"))) {
                return candidate.normalize();
            }
        }
        return null;
    }

    static void addCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    static class ApiHandler implements HttpHandler {

        private final ObjectMapper objectMapper = new ObjectMapper();
        private final PanService panEvaluator = new PanService();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_NO_CONTENT, -1);
                exchange.close();
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, -1);
                exchange.close();
                return;
            }

            try {
                JsonNode root = objectMapper.readTree(exchange.getRequestBody());
                JsonNode stateNode = root.has("state") ? root.get("state") : root;
                RequestDto request = objectMapper.treeToValue(stateNode, RequestDto.class);
                ResponseDto response = panEvaluator.evaluate(request);

                byte[] body = objectMapper.writeValueAsBytes(response);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, body.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(body);
                }
            } catch (Exception e) {
                String message = e.getMessage();
                if (message == null || message.isBlank()) {
                    message = e.getClass().getSimpleName();
                }
                byte[] err = objectMapper.writeValueAsBytes(new ErrorDto(message));
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, err.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(err);
                }
                e.printStackTrace();
            }
        }
    }

    static class StaticHandler implements HttpHandler {

        private final Path root;

        StaticHandler(Path root) {
            this.root = root;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, -1);
                exchange.close();
                return;
            }

            String rawPath = exchange.getRequestURI().getPath();
            String path = rawPath.equals("/") ? "index.html" : rawPath.replaceFirst("^/", "");
            Path file = root.resolve(path).normalize();
            if (!file.startsWith(root) || !Files.isRegularFile(file)) {
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, -1);
                exchange.close();
                return;
            }

            byte[] body = Files.readAllBytes(file);
            exchange.getResponseHeaders().set("Content-Type", contentType(file));
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        }

        private static String contentType(Path file) {
            String name = file.getFileName().toString();
            if (name.endsWith(".html")) return "text/html; charset=utf-8";
            if (name.endsWith(".js")) return "application/javascript; charset=utf-8";
            if (name.endsWith(".css")) return "text/css; charset=utf-8";
            return "application/octet-stream";
        }
    }

    static class ErrorDto {
        public String error;

        ErrorDto(String error) {
            this.error = error;
        }
    }

    public static class RequestDto {
        public Player[] players;
        public List<String> stack;
        public int currentPlayer;

        public PanState toPanState() {
            if (players == null || players.length < 2) {
                throw new IllegalArgumentException("Expected two players with hands");
            }
            List<Card> first = toCards(players[0].hand);
            List<Card> second = toCards(players[1].hand);
            List<Card> pile = toCards(stack != null ? stack : List.of());
            return new PanState(first, second, pile, currentPlayer == 0);
        }

        private List<Card> toCards(List<String> hand) {
            if (hand == null) {
                return List.of();
            }
            return hand.stream()
                    .map(this::mapCard)
                    .toList();
        }

        private Card mapCard(String str) {
            Rank rank = rank(str);
            Suit suit = suit(str);
            return Card.of(rank, suit);
        }

        private Rank rank(String str) {
            if (str.length() == 3) {
                return Rank.TEN;
            }
            for (Rank rank : Rank.values()) {
                if (rank.toString().startsWith(str.substring(0, str.length() - 1))) {
                    return rank;
                }
            }
            throw new IllegalArgumentException("Invalid card: " + str);
        }

        private static Suit suit(String str) {
            char s = str.charAt(str.length() - 1);
            return switch (s) {
                case 'H' -> Suit.HEARTS;
                case 'C' -> Suit.CLUBS;
                case 'D' -> Suit.DIAMONDS;
                case 'S' -> Suit.SPADES;
                default -> throw new IllegalArgumentException("Invalid card: " + str);
            };
        }

        public static class Player {
            public List<String> hand;

        }
    }

    public static class ResponseDto {
        public int score;
        public Object bestMove;
        /** Turns until player 1 wins, when the search proves it. */
        public Integer winIn;
        /** Turns until player 1 loses, when the search proves it. */
        public Integer loseIn;

        public ResponseDto() {
        }

        public ResponseDto(int score, Object bestMove) {
            this.score = score;
            this.bestMove = bestMove;
        }
    }
}
