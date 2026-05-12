package issueissyu.backend.domain.auth.service;

import issueissyu.backend.domain.auth.dto.req.LocalLoginReqDTO;
import issueissyu.backend.domain.auth.dto.res.LocalLoginResDTO;
import issueissyu.backend.domain.auth.exception.AuthException;
import issueissyu.backend.domain.auth.exception.code.AuthErrorCode;
import issueissyu.backend.domain.user.entity.OAuth;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.enums.SocialType;
import issueissyu.backend.domain.user.repository.OAuthRepository;
import issueissyu.backend.global.redis.RefreshTokenRedisStore;
import issueissyu.backend.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalLoginService {

    private static final String LOCAL_PROVIDER = SocialType.LOCAL.name().toLowerCase();

    private final OAuthRepository oAuthRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRedisStore refreshTokenRedisStore;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LocalLoginResDTO login(LocalLoginReqDTO req) {
        String loginId = req.getUserName().trim();

        // 로컬 OAuth 레코드 조회 (providerId = 회원가입 시 저장한 로그인 아이디)
        OAuth oauth = oAuthRepository
                .findBySocialTypeAndProviderIdWithUser(SocialType.LOCAL, loginId)
                .orElseThrow(() -> AuthException.of(AuthErrorCode.LOCAL_LOGIN_401));

        // 비밀번호 검증
        if (oauth.getPassword() == null || !passwordEncoder.matches(req.getPassword(), oauth.getPassword())) {
            throw AuthException.of(AuthErrorCode.LOCAL_LOGIN_401);
        }

        User user = oauth.getUser();
        String uid = user.getUid();

        boolean isNew = user.needsLoginOnboarding();

        // JWT 발급 + Redis 저장
        String accessToken = jwtTokenProvider.createAccessToken(uid);
        String refreshToken = jwtTokenProvider.createRefreshToken(uid, LOCAL_PROVIDER);
        refreshTokenRedisStore.save(uid, LOCAL_PROVIDER, refreshToken,
                Duration.ofMillis(jwtTokenProvider.getRefreshExpMs()));

        LocalLoginResDTO.UserInfo userInfo = isNew
                ? LocalLoginResDTO.UserInfo.builder()
                        .tempUuid(uid)
                        .userName(user.getUserName())
                        .build()
                : LocalLoginResDTO.UserInfo.builder()
                        .uuid(uid)
                        .userName(user.getUserName())
                        .build();

        log.debug("로컬 로그인 완료: uid={}, isNew={}", uid, isNew);

        return LocalLoginResDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getAccessExpMs() / 1000)
                .newUser(isNew)
                .socialType(SocialType.LOCAL.name())
                .user(userInfo)
                .build();
    }
}
