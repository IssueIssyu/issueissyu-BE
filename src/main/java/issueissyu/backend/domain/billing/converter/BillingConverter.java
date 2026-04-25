package issueissyu.backend.domain.billing.converter;

import issueissyu.backend.domain.billing.dto.res.MyPurchasesRes;
import issueissyu.backend.domain.billing.dto.res.ProductRes;
import issueissyu.backend.domain.pin.entity.Emogji;
import issueissyu.backend.domain.pin.entity.mapping.UserEmogji;
import issueissyu.backend.domain.user.entity.User;

import java.util.List;

public class BillingConverter {

    // Emoji -> ProductRes 변환용
    public static ProductRes toProductRes(Emogji emogji) {
        return ProductRes.builder()
                .emojiId(emogji.getEmojiId())
                .emojiType(emogji.getEmojiType())
                .productId(emogji.getProductId())
                .emojiImageUrl(emogji.getEmojiImageUrl())
                .build();
    }

    // /products 응답 생성용
    public static List<ProductRes> toProductResList(List<Emogji> emogjiList) {
        return emogjiList.stream()
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
    public static UserEmogji toUserEmogji(User user, Emogji emogji) {
        return UserEmogji.builder()
                .user(user)
                .emogji(emogji)
                .build();
    }
}
