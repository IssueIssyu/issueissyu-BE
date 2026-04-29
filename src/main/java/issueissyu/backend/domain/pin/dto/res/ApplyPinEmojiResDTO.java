package issueissyu.backend.domain.pin.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplyPinEmojiResDTO {

    @Schema(description = "현재 선택한 이모지 ID", example = "1")
    private Long emogjiId;

    @Schema(description = "현재 선택한 이모지 이미지 URL", example = "https://cdn.example.com/emoji/smile.png")
    private String emojiImageUrl;
}
