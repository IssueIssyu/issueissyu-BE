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
        List<Emogji> emogjiList = emogjiRepository.findAll();
        return BillingConverter.toProductResList(emogjiList);
    }

    @Override
    public MyPurchasesRes getMyPurchases(String uid) {
        return BillingConverter.toMyPurchasesRes(userEmogjiRepository.findOwnedEmojiIdsByUid(uid));
    }
}
