package issueissyu.backend.domain.pin.exception.code;

import issueissyu.backend.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PinErrorCode implements BaseErrorCode {

    // 핀
    PIN_NOT_FOUND(HttpStatus.NOT_FOUND, "PIN_NOT_FOUND_404", "존재하지 않는 핀입니다."),

    // 핀 이모지 반응
    EMOJI_NOT_FOUND(HttpStatus.NOT_FOUND, "EMOJI_NOT_FOUND_404_1", "존재하지 않는 이모지입니다."),
    EMOJI_NOT_OWNED(HttpStatus.FORBIDDEN, "EMOJI_NOT_OWNED_403_1", "구매하지 않은 이모지입니다."),
    MY_EMOJI_NOT_FOUND(HttpStatus.NOT_FOUND, "MY_EMOJI_NOT_FOUND_404_2", "취소할 내 반응이 없습니다."),

    // 핀 댓글
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND_404_3", "존재하지 않는 댓글입니다."),
    COMMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "COMMENT_FORBIDDEN_403_2", "댓글 수정/삭제 권한이 없습니다."),

    // 핀 공감
    PIN_LIKE_ALREADY(HttpStatus.BAD_REQUEST, "PIN_LIKE_400_1", "이미 공감된 핀입니다."),

    // 핀 이미지 업로드
    PIN_IMAGE_400_1(HttpStatus.BAD_REQUEST, "PIN_IMAGE_400_1", "첨부한 사진 용량이 너무 큽니다."),
    PIN_IMAGE_400_2(HttpStatus.BAD_REQUEST, "PIN_IMAGE_400_2", "사진 첨부에 실패했습니다."),
    PIN_IMAGE_400_3(HttpStatus.BAD_REQUEST, "PIN_IMAGE_400_3", "최대 사진 첨부 갯수를 초과했습니다."),

    // 소통 핀 등록
    PIN_IMPORT_COMMUNICATION_400_1(HttpStatus.BAD_REQUEST, "PIN_IMPORT_COMMUNICATION_400_1", "필수 요청 값 누락."),
    PIN_IMPORT_COMMUNICATION_400_2(HttpStatus.BAD_REQUEST, "PIN_IMPORT_COMMUNICATION_400_2", "소통 핀 등록 API를 실행 할 수 없습니다."),

    // 소통 핀 수정
    PIN_EDIT_COMMUNICATION_400_1(HttpStatus.BAD_REQUEST, "PIN_EDIT_COMMUNICATION_400_1", "필수 요청 값 누락."),
    PIN_EDIT_COMMUNICATION_400_2(HttpStatus.NOT_FOUND, "PIN_EDIT_COMMUNICATION_400_2", "존재하지 않는 핀 입니다."),
    PIN_EDIT_COMMUNICATION_400_3(HttpStatus.FORBIDDEN, "PIN_EDIT_COMMUNICATION_400_3", "핀 작성자가 아니므로 수정 권한이 없습니다."),
    PIN_EDIT_COMMUNICATION_400_4(HttpStatus.BAD_REQUEST, "PIN_EDIT_COMMUNICATION_400_4", "소통(COMMUNICATION) 타입의 핀이 아닙니다."),
    PIN_EDIT_COMMUNICATION_400_5(HttpStatus.BAD_REQUEST, "PIN_EDIT_COMMUNICATION_400_5", "핀 수정 API를 실행 할 수 없습니다."),

    // 핀 삭제
    PIN_DELETE_400_1(HttpStatus.BAD_REQUEST, "PIN_DELETE_400_1", "등업된 이슈 핀은 삭제가 불가능 합니다."),
    PIN_DELETE_400_2(HttpStatus.NOT_FOUND, "PIN_DELETE_400_2", "존재하지 않는 핀 입니다."),
    PIN_DELETE_400_3(HttpStatus.FORBIDDEN, "PIN_DELETE_400_3", "핀 작성자가 아니므로 삭제 권한이 없습니다."),
    PIN_DELETE_400_4(HttpStatus.BAD_REQUEST, "PIN_DELETE_400_4", "핀 삭제 API를 실행 할 수 없습니다."),

    // 핀 상세 홈
    PIN_HOME_404(HttpStatus.NOT_FOUND, "PIN_HOME_404", "존재하지 않는 핀 입니다."),
    PIN_HOME_400(HttpStatus.BAD_REQUEST, "PIN_HOME_400", "핀 상세 홈 조회 API를 실행 할 수 없습니다."),

    // 핀 상세 포스트
    PIN_POST_404(HttpStatus.NOT_FOUND, "PIN_POST_404", "존재하지 않는 핀 입니다."),
    PIN_POST_400(HttpStatus.BAD_REQUEST, "PIN_POST_400", "핀 상세 포스트 조회 API를 실행 할 수 없습니다."),

    // 핀 신고
    PIN_DECLARATION_404_1(HttpStatus.NOT_FOUND, "PIN_DECLARATION_404_1", "존재하지 않는 핀 입니다."),
    PIN_DECLARATION_409_1(HttpStatus.CONFLICT, "PIN_DECLARATION_409_1", "이미 신고한 핀 입니다."),

    // 핀 상세 해결하기(이슈)
    PIN_SOLVE_404(HttpStatus.NOT_FOUND, "PIN_SOLVE_404", "존재하지 않는 핀 입니다."),
    PIN_SOLVE_400(HttpStatus.BAD_REQUEST, "PIN_SOLVE_400", "핀 상세 해결하기 조회 API를 실행 할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
