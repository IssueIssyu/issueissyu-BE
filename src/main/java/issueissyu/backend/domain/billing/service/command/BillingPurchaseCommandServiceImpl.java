package issueissyu.backend.domain.billing.service.command;

import issueissyu.backend.domain.billing.converter.BillingConverter;
import issueissyu.backend.domain.billing.dto.req.VerifyPurchaseReq;
import issueissyu.backend.domain.billing.exception.code.BillingErrorCode;
import issueissyu.backend.domain.billing.repository.UserEmogjiRepository;
import issueissyu.backend.domain.pin.entity.Emogji;
import issueissyu.backend.domain.pin.entity.mapping.UserEmogji;
import issueissyu.backend.domain.pin.repository.EmogjiRepository;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BillingPurchaseCommandServiceImpl implements BillingPurchaseCommandService {

    private final UserEmogjiRepository userEmogjiRepository;
    private final EmogjiRepository emogjiRepository;
    private final UserRepository userRepository;

    @Value("${billing.google-verification-enabled:false}")
    private boolean googleVerificationEnabled;
    @Override
    public Long verifyPurchase(String uid, VerifyPurchaseReq request) {
        Emogji emogji = emogjiRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> GeneralException.of(BillingErrorCode.PRODUCT_NOT_FOUND));

        if (userEmogjiRepository.existsByUserUidAndEmogjiEmojiId(uid, emogji.getEmojiId())) {
            throw GeneralException.of(BillingErrorCode.PURCHASE_ALREADY_PROCESSED);
        }

        User user = userRepository.findById(uid)
                .orElseThrow(() -> GeneralException.of(BillingErrorCode.PURCHASE_TOKEN_INVALID));

        if (googleVerificationEnabled) {
            verifyWithGooglePlay(request.getProductId(), request.getPurchaseToken());
        }

        UserEmogji savedUserEmogji = userEmogjiRepository.save(BillingConverter.toUserEmogji(user, emogji));
        return savedUserEmogji.getEmogji().getEmojiId();
    }

    private void verifyWithGooglePlay(String productId, String purchaseToken) {
        //프론트 연결 후 삭제
        // Google Play 실검증은 프론트 결제 흐름/콘솔 연동 완료 후 활성화한다.
        // 아래 블록 주석 해제 시 동작하도록 코드만 미리 작성해 둠.
        throw GeneralException.of(BillingErrorCode.GOOGLE_PLAY_API_ERROR);

        /*
        AndroidPublisher androidPublisher = androidPublisherProvider.getIfAvailable();
        if (androidPublisher == null) {
            log.error("[Billing] AndroidPublisher Bean이 없습니다. 설정/의존성을 확인하세요.");
            throw GeneralException.of(BillingErrorCode.GOOGLE_PLAY_API_ERROR);
        }

        if (packageName == null || packageName.isBlank()) {
            log.error("[Billing] google.play.package-name 설정이 비어 있습니다.");
            throw GeneralException.of(BillingErrorCode.GOOGLE_PLAY_API_ERROR);
        }

        try {
            AndroidPublisher.Purchases.Products.Get request = androidPublisher.purchases()
                    .products()
                    .get(packageName, productId, purchaseToken);

            ProductPurchase purchase = request.execute();
            Integer purchaseState = purchase.getPurchaseState();

            // 0 = Purchased
            if (purchaseState == null || purchaseState != 0) {
                throw GeneralException.of(BillingErrorCode.PURCHASE_TOKEN_INVALID);
            }

            // consume/acknowledge는 앱 정책 확정 후 추가
        } catch (GoogleJsonResponseException e) {
            int statusCode = e.getStatusCode();
            if (statusCode == 400 || statusCode == 404 || statusCode == 410) {
                throw GeneralException.of(BillingErrorCode.PURCHASE_TOKEN_INVALID);
            }
            log.error("[Billing] Google Play API 응답 오류 - status: {}, body: {}", statusCode, e.getDetails());
            throw GeneralException.of(BillingErrorCode.GOOGLE_PLAY_API_ERROR);
        } catch (IOException e) {
            log.error("[Billing] Google Play API 통신 실패 - {}", e.getMessage());
            throw GeneralException.of(BillingErrorCode.GOOGLE_PLAY_API_ERROR);
        }
        */
    }
}
