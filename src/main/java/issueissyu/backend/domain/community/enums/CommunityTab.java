package issueissyu.backend.domain.community.enums;

import issueissyu.backend.domain.community.exception.CommunityException;
import issueissyu.backend.domain.community.exception.code.CommunityErrorCode;

import java.util.Locale;

public enum CommunityTab {
    HOT,
    ISSUE,
    STORE,
    FESTIVAL,
    POLICY,
    CONTEST,
    CARDNEWS,
    ALL,
    COMMUNICATION;

    public static CommunityTab parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return ALL;
        }
        try {
            return CommunityTab.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw CommunityException.of(CommunityErrorCode.COMMUNITY_400_1);
        }
    }
}
