package pan.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import pan.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.List;

public class Server {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/evaluate", new ApiHandler());
        server.start();
        System.out.println("Server started on http://localhost:" + server.getAddress().getPort());
    }

    static class ApiHandler implements HttpHandler {

        private final ObjectMapper objectMapper = new ObjectMapper();
        private final PanService panEvaluator = new PanService();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                RequestDto request = objectMapper.readValue(exchange.getRequestBody(), RequestDto.class);
                ResponseDto response = panEvaluator.evaluate(request);

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                OutputStream os = exchange.getResponseBody();
                objectMapper.writeValue(os, response);
                os.close();
            } catch (Exception e) {
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0);
                e.printStackTrace();
            }
        }
    }

    public static class RequestDto {
        Player[] players;
        List<String> stack;
        int currentPlayer;

        public PanState toPanState() {
            return new PanState(
                    toCards(players[0].hand),
                    toCards(players[1].hand),
                    toCards(stack),
                    currentPlayer == 0
            );
        }

        private List<Card> toCards(List<String> hand) {
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
                if (rank.toString().startsWith(str)) {
                    return rank;
                }
            }
            throw new IllegalArgumentException(str);
        }

        private static Suit suit(String str) {
            char s = str.charAt(str.length() - 1);
            return switch (s) {
                case 'H' -> Suit.HEARTS;
                case 'C' -> Suit.CLUBS;
                case 'D' -> Suit.DIAMONDS;
                case 'S' -> Suit.SPADES;
                default -> throw new IllegalArgumentException(str);
            };
        }

        public static class Player {
            List<String> hand;

        }
    }

    public static class ResponseDto {
        public int score;
        public Object bestMove;

        public ResponseDto() {
        }

        public ResponseDto(int score, Object bestMove) {
            this.score = score;
            this.bestMove = bestMove;
        }
    }
}