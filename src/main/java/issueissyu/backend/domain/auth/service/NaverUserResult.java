package issueissyu.backend.domain.auth.service;

import issueissyu.backend.domain.user.entity.User;

// 네이버 유저 조회/생성 결과 — OAuth2 콜백과 직접 API 양쪽에서 공용 사용
public record NaverUserResult(User user, boolean isNew) {
}
