package issueissyu.backend.domain.alarm.exception;

import issueissyu.backend.global.api.code.BaseErrorCode;
import issueissyu.backend.global.exception.GeneralException;

public class AlarmException extends GeneralException {

    public AlarmException(BaseErrorCode code) {
        super(code);
    }

    public static AlarmException of(BaseErrorCode code) {
        return new AlarmException(code);
    }
}
