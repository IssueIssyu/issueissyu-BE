package issueissyu.backend.domain.pin.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PinEmojiSummaryResDTO {

    @Schema(description = "이모지 ID", example = "1")
    private Long emojiId;

    @Schema(description = "이모지 이미지 URL", example = "https://cdn.example.com/emoji/smile.png")
    private String emojiImageUrl;

    @Schema(description = "해당 이모지 반응 수", example = "12")
    private int count;

    @Schema(description = "기본 제공 이모지 여부", example = "true")
    private boolean isDefault;

    @Schema(description = "사용자 보유 이모지 여부", example = "true")
    private boolean isOwned;

    @Schema(description = "구글 플레이 상품 ID (미보유 유료 이모지 구매 시 사용, 기본/보유 이모지는 null)", example = "emoji_sparkle")
    private String productId;
}
