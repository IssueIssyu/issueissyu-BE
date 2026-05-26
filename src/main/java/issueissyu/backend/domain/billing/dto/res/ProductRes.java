package issueissyu.backend.domain.billing.dto.res;

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
    private String productId;
    private String emojiImageUrl;
    private boolean isDefault;
}
