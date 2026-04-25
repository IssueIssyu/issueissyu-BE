package issueissyu.backend.domain.billing.exception.code;

import issueissyu.backend.global.api.code.BaseErrorCode;
import issueissyu.backend.global.api.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BillingErrorCode implements BaseErrorCode {

    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "BILLING_4041", "존재하지 않는 상품입니다."),
    PURCHASE_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "BILLING_4001", "유효하지 않은 purchaseToken입니다."),
    PURCHASE_ALREADY_PROCESSED(HttpStatus.CONFLICT, "BILLING_4091", "이미 처리된 결제입니다."),
    GOOGLE_PLAY_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "BILLING_5001", "Google Play API 통신 중 오류가 발생했습니다.");

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
