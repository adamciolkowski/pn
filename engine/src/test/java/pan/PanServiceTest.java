package pan;

import org.junit.jupiter.api.Test;
import pan.http.Server;

import static org.assertj.core.api.Assertions.assertThat;

class PanServiceTest {

    @Test
    void mateScoreBecomesPlyDistance() {
        Server.ResponseDto win = new Server.ResponseDto();
        PanService.applyMateDistance(win, 105);
        assertThat(win.winIn).isEqualTo(1);
        assertThat(win.loseIn).isNull();

        Server.ResponseDto loss = new Server.ResponseDto();
        PanService.applyMateDistance(loss, -104);
        assertThat(loss.loseIn).isEqualTo(2);
        assertThat(loss.winIn).isNull();
    }

    @Test
    void nonMateScoreHasNoDistance() {
        Server.ResponseDto dto = new Server.ResponseDto();
        PanService.applyMateDistance(dto, 0);
        assertThat(dto.winIn).isNull();
        assertThat(dto.loseIn).isNull();
    }
}
