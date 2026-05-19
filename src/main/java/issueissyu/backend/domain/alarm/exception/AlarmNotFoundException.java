package issueissyu.backend.domain.alarm.exception;

import issueissyu.backend.global.api.code.BaseErrorCode;

public class AlarmNotFoundException extends AlarmException {

    public AlarmNotFoundException(BaseErrorCode code) {
        super(code);
    }
}
