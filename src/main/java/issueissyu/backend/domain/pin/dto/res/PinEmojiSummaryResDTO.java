package issueissyu.backend.domain.pin.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PinEmojiSummaryResDTO {

    @Schema(description = "이모지 ID", example = "1")
    private Long emogjiId;

    @Schema(description = "이모지 이미지 URL", example = "https://cdn.example.com/emoji/smile.png")
    private String emojiImageUrl;

    @Schema(description = "해당 이모지 반응 수", example = "12")
    private int count;

    @Schema(description = "내가 선택한 이모지인지 여부", example = "true")
    private boolean isMine;
}
