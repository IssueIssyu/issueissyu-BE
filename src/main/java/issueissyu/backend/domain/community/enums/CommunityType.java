package issueissyu.backend.domain.community.enums;

import issueissyu.backend.domain.community.exception.CommunityException;
import issueissyu.backend.domain.community.exception.code.CommunityErrorCode;

import java.util.Locale;
import java.util.Optional;

public enum CommunityType {
    ISSUE,
    STORE,
    POLICY,
    FESTIVAL,
    CONTEST,
    COMMUNICATION,
    CARDNEWS;

    public static Optional<CommunityType> parseOptional(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(raw.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            throw CommunityException.of(CommunityErrorCode.COMMUNITY_400_1);
        }
    }
}
