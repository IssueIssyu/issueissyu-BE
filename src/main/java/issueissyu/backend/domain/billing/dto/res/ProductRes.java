package issueissyu.backend.domain.billing.dto.res;

import issueissyu.backend.domain.pin.enums.EmojiType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRes {
    private Long emojiId;
    private EmojiType emojiType;
    private String productId;
    private String emojiImageUrl;
    private boolean isDefault;
}
