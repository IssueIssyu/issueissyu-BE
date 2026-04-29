package issueissyu.backend.domain.pin.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmojiCandidateResDTO {

    @Schema(description = "이모지 ID", example = "1")
    private Long emogjiId;

    @Schema(description = "이모지 이미지 URL", example = "https://cdn.example.com/emoji/smile.png")
    private String emojiImageUrl;

    @Schema(description = "기본 제공 이모지 여부", example = "true")
    private boolean isDefault;

    @Schema(description = "사용자 보유 이모지 여부", example = "true")
    private boolean isOwned;

    @Schema(description = "현재 내 반응인지 여부", example = "false")
    private boolean isMine;
}
