package issueissyu.backend.domain.issue.exception;

import issueissyu.backend.domain.issue.exception.code.IssueErrorCode;
import issueissyu.backend.global.api.code.BaseErrorCode;
import lombok.Getter;

@Getter
public class PetitionServerException extends RuntimeException {

    private final BaseErrorCode code;

    private PetitionServerException(BaseErrorCode code, Throwable cause) {
        super(cause != null ? cause.getMessage() : code.getMessage(), cause);
        this.code = code;
    }

    public static PetitionServerException submitFailed(Throwable cause) {
        return new PetitionServerException(IssueErrorCode.PETITION_500, cause);
    }

    public static PetitionServerException statusLookupFailed(Throwable cause) {
        return new PetitionServerException(IssueErrorCode.PETITION_STATUS_500, cause);
    }
}
