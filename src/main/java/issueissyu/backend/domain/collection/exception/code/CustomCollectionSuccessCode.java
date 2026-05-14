package issueissyu.backend.domain.collection.exception.code;

import issueissyu.backend.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CustomCollectionSuccessCode implements BaseSuccessCode {
    USER_COLLECTIONS_GET_SUCCESS(HttpStatus.OK, "USER_COLLECTIONS_GET_SUCCESS", "컬렉션 목록 조회에 성공했습니다."),
    PROFILE_COLLECTION_UPDATE_SUCCESS(HttpStatus.OK, "PROFILE_COLLECTION_UPDATE_SUCCESS", "프로필 컬렉션이 변경되었습니다."),
    CUSTOM_COLLECTION_BOOKMARK_UPDATE_SUCCESS(HttpStatus.OK, "CUSTOM_COLLECTION_BOOKMARK_UPDATE_SUCCESS", "컬렉션 북마크 상태가 변경되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
