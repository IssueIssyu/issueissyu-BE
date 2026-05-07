package issueissyu.backend.domain.issue.exception.code;

import issueissyu.backend.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum IssueSuccessCode implements BaseSuccessCode {

    PETITION_200(HttpStatus.OK, "PETITION_200", "청원에 성공했습니다."),
    PETITION_STATUS_200(HttpStatus.OK, "PETITION_STATUS_200", "청원 현황 조회에 성공했습니다."),

    GO_NOW_200(HttpStatus.OK, "GO_NOW_200", "시민해결사 참여에 성공했습니다."),

    PROBLEM_SOLVER_200_1(HttpStatus.OK, "PROBLEM_SOLVER_200_1", "시민해결사 목록 조회에 성공했습니다."),
    PROBLEM_SOLVER_200_2(HttpStatus.OK, "PROBLEM_SOLVER_200_2", "내 핀 시민해결사 목록 조회에 성공했습니다."),
    PROBLEM_SOLVER_PHOTO_200(HttpStatus.OK, "PROBLEM_SOLVER_PHOTO_200", "시민해결사 인증 사진이 성공적으로 등록되었습니다."),
    PROBLEM_SOLVER_CHECK_200(HttpStatus.OK, "PROBLEM_SOLVER_CHECK_200", "시민해결사 인증에 성공적했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
