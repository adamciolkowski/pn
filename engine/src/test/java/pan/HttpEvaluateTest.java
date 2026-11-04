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
        request.players = new Server.RequestDto.Player[] {
                player("10S", "QH", "QD", "QC", "QS", "KH", "KD", "KC", "KS", "AH", "AD", "AC"),
                player("AS"),
        };
        request.stack = List.of(
                "9H", "9S", "9D", "9C", "10C", "10H", "10D", "JD", "JC", "JS", "JH"
        );

        Server.ResponseDto response = new PanService().evaluate(request);

        assertThat(response.score).isPositive();
        assertThat(response.bestMove).isEqualTo(Map.of("type", "one", "card", "AS"));
    }

    private static Server.RequestDto.Player player(String... cards) {
        Server.RequestDto.Player p = new Server.RequestDto.Player();
        p.hand = List.of(cards);
        return p;
    }
}
