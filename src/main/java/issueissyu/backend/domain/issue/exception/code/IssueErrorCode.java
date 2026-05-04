package issueissyu.backend.domain.issue.exception.code;

import issueissyu.backend.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum IssueErrorCode implements BaseErrorCode {

    PETITION_400_1(HttpStatus.BAD_REQUEST, "PETITION_400_1", "이미 청원되었습니다."),
    PETITION_400_2(HttpStatus.BAD_REQUEST, "PETITION_400_2", "청원이 가능한 핀 종류가 아닙니다."),
    PETITION_404(HttpStatus.NOT_FOUND, "PETITION_404", "존재하지 않는 핀 입니다."),
    PETITION_500(HttpStatus.INTERNAL_SERVER_ERROR, "PETITION_500", "청원하기 중 서버 오류가 발생했습니다."),
    PETITION_STATUS_404(HttpStatus.NOT_FOUND, "PETITION_STATUS_404", "존재하지 않는 핀 입니다."),
    PETITION_STATUS_500(HttpStatus.INTERNAL_SERVER_ERROR, "PETITION_STATUS_500", "청원하기 중 서버 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
