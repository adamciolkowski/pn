package pan;

import org.junit.jupiter.api.Test;
import pan.http.Server;

import static org.assertj.core.api.Assertions.assertThat;

class PanServiceTest {

    @Test
    void winScoreBecomesTurnDistance() {
        Server.ResponseDto win = new Server.ResponseDto();
        PanService.applyWinDistance(win, 105);
        assertThat(win.winIn).isEqualTo(1);
        assertThat(win.loseIn).isNull();

        Server.ResponseDto loss = new Server.ResponseDto();
        PanService.applyWinDistance(loss, -104);
        assertThat(loss.loseIn).isEqualTo(2);
        assertThat(loss.winIn).isNull();
    }

    @Test
    void nonWinScoreHasNoTurnDistance() {
        Server.ResponseDto dto = new Server.ResponseDto();
        PanService.applyWinDistance(dto, 0);
        assertThat(dto.winIn).isNull();
        assertThat(dto.loseIn).isNull();
    }
}
