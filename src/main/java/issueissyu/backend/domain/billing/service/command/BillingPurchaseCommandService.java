package issueissyu.backend.domain.billing.service.command;

import issueissyu.backend.domain.billing.dto.req.VerifyPurchaseReq;

public interface BillingPurchaseCommandService {
    Long verifyPurchase(String uid, VerifyPurchaseReq request);
}
