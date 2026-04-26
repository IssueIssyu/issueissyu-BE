package issueissyu.backend.domain.billing.service.query;

import issueissyu.backend.domain.billing.converter.BillingConverter;
import issueissyu.backend.domain.billing.dto.res.MyPurchasesRes;
import issueissyu.backend.domain.billing.dto.res.ProductRes;
import issueissyu.backend.domain.billing.repository.UserEmogjiRepository;
import issueissyu.backend.domain.pin.entity.Emogji;
import issueissyu.backend.domain.pin.repository.EmogjiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BillingQueryServiceImpl implements BillingQueryService {

    private final EmogjiRepository emogjiRepository;
    private final UserEmogjiRepository userEmogjiRepository;

    @Override
    public List<ProductRes> getProducts() {
        // 전체 이모티콘 목록을 반환, 잠금 판단은 isDefault/owned 조합으로 프론트에서 처리
        List<Emogji> emogjiList = emogjiRepository.findAllByOrderByEmojiIdAsc();
        return BillingConverter.toProductResList(emogjiList);
    }

    @Override
    public MyPurchasesRes getMyPurchases(String uid) {
        return BillingConverter.toMyPurchasesRes(userEmogjiRepository.findOwnedEmojiIdsByUid(uid));
    }
}
