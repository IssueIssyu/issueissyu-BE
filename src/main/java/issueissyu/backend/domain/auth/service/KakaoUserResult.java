package issueissyu.backend.domain.auth.service;

import issueissyu.backend.domain.user.entity.User;

public record KakaoUserResult(User user, boolean isNew) {
}
