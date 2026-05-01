package issueissyu.backend.domain.pin.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplyPinEmojiResDTO {

    @Schema(description = "토글 처리 후 현재 내가 선택한 이모지 ID (선택 해제 시 null)", example = "1", nullable = true)
    private Long selectedEmojiId;
}
