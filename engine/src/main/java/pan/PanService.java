package pan;

import pan.http.Server;

public class PanService {

    public Server.ResponseDto evaluate(Server.RequestDto request) {
        PanEvaluator panEvaluator = new PanEvaluator();
        PanEvaluator.MoveAndScore moveAndScore = panEvaluator.findBestMove(request.toPanState(), 6);
        return toResponseDto(moveAndScore);
    }

    private Server.ResponseDto toResponseDto(PanEvaluator.MoveAndScore moveAndScore) {
        Server.ResponseDto response = new Server.ResponseDto();
        response.bestMove = moveAndScore.move();
        response.score = moveAndScore.score();
        return response;
    }
}
