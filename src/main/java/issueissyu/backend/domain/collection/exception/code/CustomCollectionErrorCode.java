package issueissyu.backend.domain.collection.exception.code;

import issueissyu.backend.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CustomCollectionErrorCode implements BaseErrorCode {
    CUSTOM_COLLECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "CUSTOM_COLLECTION_NOT_FOUND", "컬렉션을 찾을 수 없습니다."),
    CUSTOM_COLLECTION_LOCKED(HttpStatus.CONFLICT, "CUSTOM_COLLECTION_LOCKED", "잠금 상태인 컬렉션입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
