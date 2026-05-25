package issueissyu.backend.domain.billing.converter;

import issueissyu.backend.domain.billing.dto.res.MyPurchasesRes;
import issueissyu.backend.domain.billing.dto.res.ProductRes;
import issueissyu.backend.domain.pin.entity.Emoji;
import issueissyu.backend.domain.pin.entity.mapping.UserEmoji;
import issueissyu.backend.domain.user.entity.User;

import java.util.List;

public class BillingConverter {

    private BillingConverter() {
        throw new IllegalStateException("Utility class");
    }

    // Emoji -> ProductRes 변환용
    public static ProductRes toProductRes(Emoji emoji) {
        return ProductRes.builder()
                .emojiId(emoji.getEmojiId())
                .productId(emoji.getProductId())
                .emojiImageUrl(emoji.getEmojiImageUrl())
                .isDefault(emoji.isDefault())
                .build();
    }

    // /products 응답 생성용
    public static List<ProductRes> toProductResList(List<Emoji> emojiList) {
        return emojiList.stream()
                .map(BillingConverter::toProductRes)
                .toList();
    }

    // /purchases/me 응답 생성용
    public static MyPurchasesRes toMyPurchasesRes(List<Long> ownedEmojiIds) {
        return MyPurchasesRes.builder()
                .ownedEmojiIds(ownedEmojiIds)
                .build();
    }

    // 결제 검증 성공 DB insert 용
    public static UserEmoji toUserEmoji(User user, Emoji emoji, String purchaseToken) {
        return UserEmoji.builder()
                .user(user)
                .emoji(emoji)
                .purchaseToken(purchaseToken)
                .build();
    }
}
