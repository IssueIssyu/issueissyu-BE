package issueissyu.backend.domain.pin.exception.code;

import issueissyu.backend.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PinSuccessCode implements BaseSuccessCode {

    // 핀 이모지 반응
    PIN_EMOJIS_200(HttpStatus.OK, "PIN_EMOJIS_200", "핀 반응 목록 조회에 성공했습니다."),
    EMOJI_CANDIDATES_200(HttpStatus.OK, "EMOJI_CANDIDATES_200", "반응 후보 목록 조회에 성공했습니다."),
    APPLY_EMOJI_200(HttpStatus.OK, "APPLY_EMOJI_200", "핀 반응 등록에 성공했습니다."),
    DELETE_EMOJI_200(HttpStatus.OK, "DELETE_EMOJI_200", "핀 반응 취소에 성공했습니다."),

    // 핀 댓글
    PIN_COMMENTS_200(HttpStatus.OK, "PIN_COMMENTS_200", "핀 댓글 목록 조회에 성공했습니다."),
    CREATE_COMMENT_200(HttpStatus.OK, "CREATE_COMMENT_200", "핀 댓글 작성에 성공했습니다."),
    UPDATE_COMMENT_200(HttpStatus.OK, "UPDATE_COMMENT_200", "핀 댓글 수정에 성공했습니다."),
    DELETE_COMMENT_200(HttpStatus.OK, "DELETE_COMMENT_200", "핀 댓글 삭제에 성공했습니다."),

    // 핀 공감
    PIN_LIKE_200(HttpStatus.OK, "PIN_LIKE_200", "핀 공감에 성공했습니다."),

    // 핀 이미지 업로드
    PIN_IMAGE_200(HttpStatus.OK, "PIN_IMAGE_200", "핀 이미지 등록에 성공했습니다."),

    // 소통 핀 등록
    PIN_IMPORT_COMMUNICATION_200(HttpStatus.OK, "PIN_IMPORT_COMMUNICATION_200", "소통 핀 등록에 성공했습니다."),

    // 소통 핀 수정
    PIN_EDIT_COMMUNICATION_200(HttpStatus.OK, "PIN_EDIT_COMMUNICATION_200", "소통 핀 수정에 성공했습니다."),

    // 핀 삭제
    PIN_DELETE_200(HttpStatus.OK, "PIN_DELETE_200", "핀 삭제에 성공했습니다."),

    // 핀 상세 홈
    PIN_HOME_200_1(HttpStatus.OK, "PIN_HOME_200_1", "이슈 핀 상세 홈 조회에 성공했습니다."),
    PIN_HOME_200_2(HttpStatus.OK, "PIN_HOME_200_2", "소통 핀 상세 홈 조회에 성공했습니다."),
    PIN_HOME_200_3(HttpStatus.OK, "PIN_HOME_200_3", "가게 핀 상세 홈 조회에 성공했습니다."),
    PIN_HOME_200_4(HttpStatus.OK, "PIN_HOME_200_4", "축제 핀 상세 홈 조회에 성공했습니다."),

    // 핀 상세 포스트
    PIN_POST_200_1(HttpStatus.OK, "PIN_POST_200_1", "이슈 핀 상세 포스트 조회에 성공했습니다."),
    PIN_POST_200_2(HttpStatus.OK, "PIN_POST_200_2", "소통 핀 상세 포스트 조회에 성공했습니다."),
    PIN_POST_200_3(HttpStatus.OK, "PIN_POST_200_3", "가게 핀 상세 포스트 조회에 성공했습니다."),
    PIN_POST_200_4(HttpStatus.OK, "PIN_POST_200_4", "축제 핀 상세 포스트 조회에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
