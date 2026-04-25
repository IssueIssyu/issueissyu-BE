package issueissyu.backend.domain.billing.exception.code;

import issueissyu.backend.global.api.code.BaseSuccessCode;
import issueissyu.backend.global.api.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BillingSuccessCode implements BaseSuccessCode {

    GET_PRODUCTS_SUCCESS(HttpStatus.OK, "BILLING_200_1", "상품 목록 조회에 성공했습니다."),
    VERIFY_PURCHASE_SUCCESS(HttpStatus.OK, "BILLING_200_2", "결제 검증에 성공했습니다."),
    GET_MY_PURCHASES_SUCCESS(HttpStatus.OK, "BILLING_200_3", "구매 내역 조회에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ReasonDTO getReason() {
        return ReasonDTO.builder()
                .httpStatus(this.httpStatus)
                .code(this.code)
                .message(this.message)
                .build();
    }
}
