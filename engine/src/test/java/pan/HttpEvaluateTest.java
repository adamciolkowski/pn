package pan;

import org.junit.jupiter.api.Test;
import pan.http.Server;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HttpEvaluateTest {

    @Test
    void currentPlayerOneCanWinInOne_moveIsLastCard() {
        Server.RequestDto request = new Server.RequestDto();
        request.currentPlayer = 1;
        request.players = List.of(
                List.of("10S", "QH", "QD", "QC", "QS", "KH", "KD", "KC", "KS", "AH", "AD", "AC"),
                List.of("AS")
        );
        request.stack = List.of(
                "9H", "9S", "9D", "9C", "10C", "10H", "10D", "JD", "JC", "JS", "JH"
        );

        Server.ResponseDto response = new PanService().evaluate(request);

        assertThat(response.score).isLessThan(0);
        assertThat(response.loseIn).isEqualTo(1);
        assertThat(response.bestMove).isEqualTo(Map.of("type", "one", "card", "AS"));
    }
}
