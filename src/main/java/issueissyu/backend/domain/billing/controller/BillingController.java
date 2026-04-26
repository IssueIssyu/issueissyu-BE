package issueissyu.backend.domain.billing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.billing.dto.req.VerifyPurchaseReq;
import issueissyu.backend.domain.billing.dto.res.MyPurchasesRes;
import issueissyu.backend.domain.billing.dto.res.ProductRes;
import issueissyu.backend.domain.billing.exception.code.BillingSuccessCode;
import issueissyu.backend.domain.billing.service.BillingFacade;
import issueissyu.backend.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
@Tag(name = "Billing", description = "인앱 결제/이모티콘 지급")
public class BillingController {

    private final BillingFacade billingFacade;

    @Operation(summary = "이모티콘 상품 목록 조회")
    @GetMapping("/products")
    public ApiResponse<List<ProductRes>> getProducts() {
        return ApiResponse.onSuccess(
                BillingSuccessCode.GET_PRODUCTS_SUCCESS,
                billingFacade.getProducts()
        );
    }

    @Operation(summary = "결제 검증 및 이모티콘 지급")
    @PostMapping("/purchases/verify")
    public ApiResponse<Long> verifyPurchase(
            @AuthenticationPrincipal String uid,
            @Valid @RequestBody VerifyPurchaseReq request
    ) {
        return ApiResponse.onSuccess(
                BillingSuccessCode.VERIFY_PURCHASE_SUCCESS,
                billingFacade.verifyPurchase(uid, request)
        );
    }

    @Operation(summary = "내 구매 이모티콘 목록 조회")
    @GetMapping("/purchases/me")
    public ApiResponse<MyPurchasesRes> getMyPurchases(@AuthenticationPrincipal String uid) {
        return ApiResponse.onSuccess(
                BillingSuccessCode.GET_MY_PURCHASES_SUCCESS,
                billingFacade.getMyPurchases(uid)
        );
    }
}
