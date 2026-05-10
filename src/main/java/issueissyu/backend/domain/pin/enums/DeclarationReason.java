package issueissyu.backend.domain.pin.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeclarationReason {
    FALSE_INFORMATION(1, "거짓 정보를 포함한 글이에요"),
    ABUSIVE_LANGUAGE(2, "욕설 또는 비하 표현이 포함된 글이에요"),
    SPAM(3, "스팸 또는 도배성 글이에요"),
    OFFENSIVE_CONTENT(4, "불쾌감을 주는 글이에요"),
    RELIGIOUS_PROSELYTIZING(5, "종교 포교 목적의 글이에요");

    private final int index;
    private final String description;

    public static DeclarationReason fromIndex(int index) {
        for (DeclarationReason reason : values()) {
            if (reason.index == index) {
                return reason;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 신고 사유 인덱스: " + index);
    }
}
