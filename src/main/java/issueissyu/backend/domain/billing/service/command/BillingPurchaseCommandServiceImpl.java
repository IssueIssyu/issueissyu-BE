package issueissyu.backend.domain.billing.service.command;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.model.ProductPurchase;
import com.google.api.services.androidpublisher.model.ProductPurchasesAcknowledgeRequest;
import issueissyu.backend.domain.billing.converter.BillingConverter;
import issueissyu.backend.domain.billing.dto.req.VerifyPurchaseReq;
import issueissyu.backend.domain.billing.exception.BillingException;
import issueissyu.backend.domain.billing.exception.code.BillingErrorCode;
import issueissyu.backend.domain.billing.repository.UserEmojiRepository;
import issueissyu.backend.domain.pin.entity.Emoji;
import issueissyu.backend.domain.pin.entity.mapping.UserEmoji;
import issueissyu.backend.domain.pin.repository.EmojiRepository;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BillingPurchaseCommandServiceImpl implements BillingPurchaseCommandService {

    private final UserEmojiRepository userEmojiRepository;
    private final EmojiRepository emojiRepository;
    private final UserRepository userRepository;
    private final ObjectProvider<AndroidPublisher> androidPublisherProvider;

    @Value("${billing.google-verification-enabled:false}")
    private boolean googleVerificationEnabled;
    @Value("${google.play.package-name:}")
    private String packageName;

    @Override
    public Long verifyPurchase(String uid, VerifyPurchaseReq request) {
        if (userEmojiRepository.existsByPurchaseToken(request.getPurchaseToken())) {
            throw BillingException.of(BillingErrorCode.PURCHASE_ALREADY_PROCESSED);
        }

        Emoji emoji = emojiRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> BillingException.of(BillingErrorCode.PRODUCT_NOT_FOUND));

        if (userEmojiRepository.existsByUserUidAndEmojiEmojiId(uid, emoji.getEmojiId())) {
            throw BillingException.of(BillingErrorCode.PURCHASE_ALREADY_PROCESSED);
        }

        User user = userRepository.findById(uid)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        if (googleVerificationEnabled) {
            verifyWithGooglePlay(request.getProductId(), request.getPurchaseToken());
        }

        UserEmoji savedUserEmoji = userEmojiRepository.save(
                BillingConverter.toUserEmoji(user, emoji, request.getPurchaseToken())
        );
        return savedUserEmoji.getEmoji().getEmojiId();
    }

    private void verifyWithGooglePlay(String productId, String purchaseToken) {
        AndroidPublisher androidPublisher = androidPublisherProvider.getIfAvailable();
        if (androidPublisher == null) {
            log.error("[Billing] AndroidPublisher Bean이 없습니다. 설정/의존성을 확인하세요.");
            throw BillingException.of(BillingErrorCode.GOOGLE_PLAY_API_ERROR);
        }

        if (packageName == null || packageName.isBlank()) {
            log.error("[Billing] google.play.package-name 설정이 비어 있습니다.");
            throw BillingException.of(BillingErrorCode.GOOGLE_PLAY_API_ERROR);
        }

        try {
            AndroidPublisher.Purchases.Products.Get request = androidPublisher.purchases()
                    .products()
                    .get(packageName, productId, purchaseToken);

            ProductPurchase purchase = request.execute();
            Integer purchaseState = purchase.getPurchaseState();

            // 0 = Purchased
            if (purchaseState == null || purchaseState != 0) {
                throw BillingException.of(BillingErrorCode.PURCHASE_TOKEN_INVALID);
            }

            if (purchase.getProductId() != null && !productId.equals(purchase.getProductId())) {
                throw BillingException.of(BillingErrorCode.PURCHASE_TOKEN_INVALID);
            }

            acknowledgeIfNeeded(androidPublisher, productId, purchaseToken, purchase);
        } catch (GoogleJsonResponseException e) {
            int statusCode = e.getStatusCode();
            if (statusCode == 400 || statusCode == 404 || statusCode == 410) {
                throw BillingException.of(BillingErrorCode.PURCHASE_TOKEN_INVALID);
            }
            log.error("[Billing] Google Play API 응답 오류 - status: {}, body: {}", statusCode, e.getDetails());
            throw BillingException.of(BillingErrorCode.GOOGLE_PLAY_API_ERROR);
        } catch (IOException e) {
            log.error("[Billing] Google Play API 통신 실패 - {}", e.getMessage());
            throw BillingException.of(BillingErrorCode.GOOGLE_PLAY_API_ERROR);
        }
    }

    private void acknowledgeIfNeeded(
            AndroidPublisher androidPublisher,
            String productId,
            String purchaseToken,
            ProductPurchase purchase
    ) throws IOException {
        // 0 = Yet to be acknowledged, 1 = Acknowledged
        Integer acknowledgementState = purchase.getAcknowledgementState();
        if (acknowledgementState != null && acknowledgementState == 1) {
            return;
        }

        androidPublisher.purchases()
                .products()
                .acknowledge(
                        packageName,
                        productId,
                        purchaseToken,
                        new ProductPurchasesAcknowledgeRequest()
                )
                .execute();
    }
}
