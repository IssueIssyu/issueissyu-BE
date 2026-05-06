package issueissyu.backend.domain.issue.exception.code;

import issueissyu.backend.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum IssueSuccessCode implements BaseSuccessCode {

    PETITION_200(HttpStatus.OK, "PETITION_200", "청원에 성공했습니다."),
    PETITION_STATUS_200(HttpStatus.OK, "PETITION_STATUS_200", "청원 현황 조회에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
