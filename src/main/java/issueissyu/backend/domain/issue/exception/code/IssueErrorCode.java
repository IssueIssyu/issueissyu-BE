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
    PETITION_400_3(HttpStatus.BAD_REQUEST, "PETITION_400_3", "본인이 등록한 이슈에는 청원할 수 없습니다."),
    PETITION_404(HttpStatus.NOT_FOUND, "PETITION_404", "존재하지 않는 핀 입니다."),
    PETITION_500(HttpStatus.INTERNAL_SERVER_ERROR, "PETITION_500", "청원하기 중 서버 오류가 발생했습니다."),
    PETITION_STATUS_404(HttpStatus.NOT_FOUND, "PETITION_STATUS_404", "존재하지 않는 핀 입니다."),
    PETITION_STATUS_500(HttpStatus.INTERNAL_SERVER_ERROR, "PETITION_STATUS_500", "청원 현황 조회 중 서버 오류가 발생했습니다."),

    GO_NOW_400_1(HttpStatus.BAD_REQUEST, "GO_NOW_400_1", "이미 시민해결사로 참여한 핀입니다."),

    PROBLEM_SOLVER_404_1(HttpStatus.NOT_FOUND, "PROBLEM_SOLVER_404_1", "존재하지 않는 핀 입니다."),
    PROBLEM_SOLVER_404_2(HttpStatus.NOT_FOUND, "PROBLEM_SOLVER_404_2", "존재하지 않는 유저 입니다."),
    PROBLEM_SOLVER_PHOTO_404(HttpStatus.NOT_FOUND, "PROBLEM_SOLVER_PHOTO_404", "존재하지 않는 시민해결사 입니다."),
    PROBLEM_SOLVER_PHOTO_400_1(HttpStatus.BAD_REQUEST, "PROBLEM_SOLVER_PHOTO_400_1", "첨부한 사진 용량이 너무 큽니다."),
    PROBLEM_SOLVER_PHOTO_400_2(HttpStatus.BAD_REQUEST, "PROBLEM_SOLVER_PHOTO_400_2", "시민해결사 사진 첨부에 실패했습니다."),
    PROBLEM_SOLVER_CHECK_404(HttpStatus.NOT_FOUND, "PROBLEM_SOLVER_CHECK_404", "존재하지 않는 시민해결사 입니다."),
    PROBLEM_SOLVER_CHECK_400_1(HttpStatus.BAD_REQUEST, "PROBLEM_SOLVER_CHECK_400_1", "인증 가능한 진행 상태가 아닙니다."),
    PROBLEM_SOLVER_CHECK_400_2(HttpStatus.BAD_REQUEST, "PROBLEM_SOLVER_CHECK_400_2", "내 핀 시민해결사 인증에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
