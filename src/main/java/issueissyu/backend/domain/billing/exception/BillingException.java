package issueissyu.backend.domain.billing.exception;

import issueissyu.backend.global.api.code.BaseErrorCode;
import issueissyu.backend.global.exception.GeneralException;

public class BillingException extends GeneralException {

    public BillingException(BaseErrorCode code) {
        super(code);
    }

    public static BillingException of(BaseErrorCode code) {
        return new BillingException(code);
    }
}
