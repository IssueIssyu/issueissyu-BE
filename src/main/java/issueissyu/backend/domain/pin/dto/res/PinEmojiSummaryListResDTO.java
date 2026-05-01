package issueissyu.backend.domain.pin.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PinEmojiSummaryListResDTO {

    @Schema(description = "현재 내가 선택한 이모지 ID (없으면 null)", example = "2", nullable = true)
    private Long selectedEmojiId;

    @Schema(description = "핀 이모지 목록")
    private List<PinEmojiSummaryResDTO> emojis;
}
