package issueissyu.backend.domain.billing.service;

import issueissyu.backend.domain.billing.dto.req.VerifyPurchaseReq;
import issueissyu.backend.domain.billing.dto.res.MyPurchasesRes;
import issueissyu.backend.domain.billing.dto.res.ProductRes;
import issueissyu.backend.domain.billing.service.command.BillingPurchaseCommandService;
import issueissyu.backend.domain.billing.service.query.BillingQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingFacade {

    private final BillingQueryService billingQueryService;
    private final BillingPurchaseCommandService billingPurchaseCommandService;

    public List<ProductRes> getProducts() {
        return billingQueryService.getProducts();
    }

    public Long verifyPurchase(String uid, VerifyPurchaseReq request) {
        return billingPurchaseCommandService.verifyPurchase(uid, request);
    }

    public MyPurchasesRes getMyPurchases(String uid) {
        return billingQueryService.getMyPurchases(uid);
    }
}
