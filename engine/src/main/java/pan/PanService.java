package pan;

import pan.http.Server;

import java.util.LinkedHashMap;
import java.util.Map;

public class PanService {

    public Server.ResponseDto evaluate(Server.RequestDto request) {
        PanEvaluator panEvaluator = new PanEvaluator();
        PanEvaluator.MoveAndScore moveAndScore = panEvaluator.findBestMove(request.toPanState(), 6);
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
        return response;
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
