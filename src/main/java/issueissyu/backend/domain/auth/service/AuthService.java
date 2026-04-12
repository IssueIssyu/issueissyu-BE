package issueissyu.backend.domain.auth.service;

import issueissyu.backend.domain.auth.dto.req.TokenReissueReqDTO;
import issueissyu.backend.domain.auth.dto.res.TokenPairDTO;
import issueissyu.backend.domain.auth.exception.code.AuthErrorCode;
import issueissyu.backend.domain.user.entity.OAuth;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.enums.SocialType;
import issueissyu.backend.domain.user.repository.OAuthRepository;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.domain.user.util.AppUuid;
import issueissyu.backend.global.exception.GeneralException;
import issueissyu.backend.global.redis.RefreshTokenRedisStore;
import issueissyu.backend.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OAuthRepository oAuthRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRedisStore refreshTokenRedisStore;

    @Transactional
    public NaverUserResult findOrCreateNaverUser(NaverUserProfile profile) {
        String providerId = profile.id();
        if (providerId == null || providerId.isBlank()) {
            throw GeneralException.of(AuthErrorCode.NAVER_LOGIN_UNAUTHORIZED);
        }

        Optional<OAuth> existing = oAuthRepository.findBySocialTypeAndProviderIdWithUser(
                SocialType.NAVER, providerId);

        if (existing.isPresent()) {
            return new NaverUserResult(existing.get().getUser(), false);
        }

        User user = createNewNaverUser(profile);
        oAuthRepository.save(OAuth.builder()
                .user(user)
                .providerId(providerId)
                .socialType(SocialType.NAVER)
                .build());
        return new NaverUserResult(user, true);
    }

    @Transactional
    public TokenPairDTO reissue(TokenReissueReqDTO req) {
        String incoming = req.getRefreshToken();

        // 서명·만료 검증
        if (!jwtTokenProvider.validateToken(incoming)) {
            throw GeneralException.of(AuthErrorCode.REFRESH_INVALID);
        }
        // refresh 타입인지 확인
        if (!JwtTokenProvider.TOKEN_TYPE_REFRESH.equals(jwtTokenProvider.parseTokenType(incoming))) {
            throw GeneralException.of(AuthErrorCode.REFRESH_INVALID);
        }

        String uid      = jwtTokenProvider.parseUid(incoming);
        String provider = jwtTokenProvider.parseProvider(incoming);
        if (provider == null) {
            throw GeneralException.of(AuthErrorCode.REFRESH_INVALID);
        }

        // Redis 저장 토큰과 일치 여부 확인
        String stored = refreshTokenRedisStore.find(uid, provider)
                .orElseThrow(() -> GeneralException.of(AuthErrorCode.REFRESH_INVALID));
        if (!stored.equals(incoming)) {
            throw GeneralException.of(AuthErrorCode.REFRESH_INVALID);
        }

        userRepository.findById(uid)
                .orElseThrow(() -> GeneralException.of(AuthErrorCode.REFRESH_INVALID));

        // 새 토큰 발급 + Redis 갱신
        String newAccess  = jwtTokenProvider.createAccessToken(uid);
        String newRefresh = jwtTokenProvider.createRefreshToken(uid, provider);
        refreshTokenRedisStore.save(uid, provider, newRefresh,
                Duration.ofMillis(jwtTokenProvider.getRefreshExpMs()));

        return TokenPairDTO.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .build();
    }

    // 특정 소셜 로그인 로그아웃: refreshToken 에서 provider 를 파싱해 해당 키만 삭제.
    // refreshToken 이 없거나 파싱 불가 시 해당 uid 의 모든 토큰 삭제(전체 로그아웃).
    public void logout(String uid, String refreshToken) {
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            String provider = jwtTokenProvider.parseProvider(refreshToken);
            if (provider != null) {
                refreshTokenRedisStore.delete(uid, provider);
                return;
            }
        }
        refreshTokenRedisStore.deleteAll(uid);
    }

    private User createNewNaverUser(NaverUserProfile profile) {
        String uid  = AppUuid.newUid();
        String name = profile.name();
        return userRepository.save(User.builder()
                .uid(uid)
                .userName(name != null && !name.isBlank() ? name : null)
                .build());
    }
}
