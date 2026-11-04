package pan;

import pan.http.Server;

import java.util.LinkedHashMap;
import java.util.Map;

public class PanService {

    /** Must match the depth passed to {@link PanEvaluator#findBestMove}. */
    static final int SEARCH_DEPTH = 6;
    /** Terminal scores are {@code ±(WIN_SCORE + remaining depth)}; see {@link PanEvaluator}. */
    static final int WIN_SCORE = 100;

    public Server.ResponseDto evaluate(Server.RequestDto request) {
        PanEvaluator panEvaluator = new PanEvaluator();
        PanEvaluator.MoveAndScore moveAndScore = panEvaluator.findBestMove(request.toPanState(), SEARCH_DEPTH);
        return toResponseDto(request, moveAndScore);
    }

    private Server.ResponseDto toResponseDto(Server.RequestDto request, PanEvaluator.MoveAndScore moveAndScore) {
        Server.ResponseDto response = new Server.ResponseDto();
        response.bestMove = serializeMove(moveAndScore.move());
        int score = moveAndScore.score();
        response.score = score;
        applyWinDistance(response, score);
        return response;
    }

    /** Turns until a proven win or loss for player 1 ({@code 100 - score + depth}). */
    static void applyWinDistance(Server.ResponseDto response, int score) {
        if (score >= WIN_SCORE) {
            response.winIn = WIN_SCORE - score + SEARCH_DEPTH;
        } else if (score <= -WIN_SCORE) {
            response.loseIn = WIN_SCORE + score + SEARCH_DEPTH;
        }
    }

    static Object serializeMove(PanMove move) {
        if (move == null) {
            return null;
        }
        if (move instanceof PanMove.OneCard oneCard) {
            Map<String, String> dto = new LinkedHashMap<>();
            dto.put("type", "one");
            dto.put("card", cardWireId(oneCard.card()));
            return dto;
        }
        if (move instanceof PanMove.FourCards fourCards) {
            Map<String, String> dto = new LinkedHashMap<>();
            dto.put("type", "four");
            dto.put("rank", fourCards.rank().toString());
            return dto;
        }
        if (move instanceof PanMove.Take) {
            return Map.of("type", "pickup");
        }
        return move.toString();
    }

    private static String cardWireId(Card card) {
        String suit = switch (card.suit()) {
            case HEARTS -> "H";
            case CLUBS -> "C";
            case DIAMONDS -> "D";
            case SPADES -> "S";
        };
        return card.rank().toString() + suit;
    }
}
