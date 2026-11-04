package pan;

import pan.http.Server;

import java.util.LinkedHashMap;
import java.util.Map;

public class PanService {

    /** Must match the depth passed to {@link PanEvaluator#findBestMove}. */
    static final int SEARCH_DEPTH = 6;
    /** Terminal scores are {@code ±(MATE_SCORE + remaining depth)}. */
    static final int MATE_SCORE = 100;

    public Server.ResponseDto evaluate(Server.RequestDto request) {
        PanEvaluator panEvaluator = new PanEvaluator();
        PanEvaluator.MoveAndScore moveAndScore = panEvaluator.findBestMove(request.toPanState(), SEARCH_DEPTH);
        return toResponseDto(request, moveAndScore);
    }

    private Server.ResponseDto toResponseDto(Server.RequestDto request, PanEvaluator.MoveAndScore moveAndScore) {
        Server.ResponseDto response = new Server.ResponseDto();
        response.bestMove = serializeMove(moveAndScore.move());
        int score = moveAndScore.score();
        if (request.currentPlayer != 0) {
            score = -score;
        }
        response.score = score;
        applyMateDistance(response, score);
        return response;
    }

    /** Side-to-move mate distance, same conversion as engine tests ({@code 100 - score + depth}). */
    static void applyMateDistance(Server.ResponseDto response, int score) {
        if (score >= MATE_SCORE) {
            response.winIn = MATE_SCORE - score + SEARCH_DEPTH;
        } else if (score <= -MATE_SCORE) {
            response.loseIn = MATE_SCORE + score + SEARCH_DEPTH;
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
