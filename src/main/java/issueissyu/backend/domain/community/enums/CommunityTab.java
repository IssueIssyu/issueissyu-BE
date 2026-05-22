package issueissyu.backend.domain.community.enums;

import issueissyu.backend.domain.community.exception.CommunityException;
import issueissyu.backend.domain.community.exception.code.CommunityErrorCode;

import java.util.Locale;

public enum CommunityTab {
    HOME,
    HOT,
    ISSUE,
    STORE,
    FESTIVAL,
    POLICY,
    CONTEST,
    CARDNEWS,
    ALL,
    COMMUNICATION;

    // 프론트에서 넘겨주는 값 파싱
    public static CommunityTab parse(String raw) {
        // 값이 없으면 HOME 반환
        if (raw == null || raw.isBlank()) {
            return HOME;
        }
        try {
            return CommunityTab.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw CommunityException.of(CommunityErrorCode.COMMUNITY_400_1);
        }
    }
}
