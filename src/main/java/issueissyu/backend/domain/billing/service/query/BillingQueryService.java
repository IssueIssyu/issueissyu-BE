package issueissyu.backend.domain.billing.service.query;

import issueissyu.backend.domain.billing.dto.res.MyPurchasesRes;
import issueissyu.backend.domain.billing.dto.res.ProductRes;

import java.util.List;

public interface BillingQueryService {
    List<ProductRes> getProducts();

    MyPurchasesRes getMyPurchases(String uid);
}
