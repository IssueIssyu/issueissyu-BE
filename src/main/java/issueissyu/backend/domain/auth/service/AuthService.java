package issueissyu.backend.domain.auth.service;

import issueissyu.backend.domain.auth.dto.req.TokenReissueReqDTO;
import issueissyu.backend.domain.auth.dto.res.TokenPairDTO;
import issueissyu.backend.domain.auth.exception.AuthException;
import issueissyu.backend.domain.auth.exception.code.AuthErrorCode;
import issueissyu.backend.domain.user.entity.OAuth;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.enums.SocialType;
import issueissyu.backend.domain.user.repository.OAuthRepository;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.domain.user.util.AppUuid;
import issueissyu.backend.global.redis.RefreshTokenRedisStore;
import issueissyu.backend.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[A-Za-z0-9가-힣]{1,15}$");

    private final UserRepository userRepository;
    private final OAuthRepository oAuthRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRedisStore refreshTokenRedisStore;

    @Transactional
    public NaverUserResult findOrCreateDevNaverUser(NaverUserProfile profile) {
        String providerId = profile.id();
        if (providerId == null || providerId.isBlank()) {
            throw AuthException.of(AuthErrorCode.NAVER_LOGIN_UNAUTHORIZED);
        }

        Optional<OAuth> existing = oAuthRepository.findBySocialTypeAndProviderIdWithUser(
                SocialType.NAVER, providerId);

        if (existing.isPresent()) {
            return new NaverUserResult(existing.get().getUser(), false);
        }

        User user = createNewDevNaverUser(profile);
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
            throw AuthException.of(AuthErrorCode.REFRESH_INVALID);
        }
        // refresh 타입인지 확인
        if (!JwtTokenProvider.TOKEN_TYPE_REFRESH.equals(jwtTokenProvider.parseTokenType(incoming))) {
            throw AuthException.of(AuthErrorCode.REFRESH_INVALID);
        }

        String uid      = jwtTokenProvider.parseUid(incoming);
        String provider = jwtTokenProvider.parseProvider(incoming);
        if (provider == null) {
            throw AuthException.of(AuthErrorCode.REFRESH_INVALID);
        }

        // Redis 저장 토큰과 일치 여부 확인
        String stored = refreshTokenRedisStore.find(uid, provider)
                .orElseThrow(() -> AuthException.of(AuthErrorCode.REFRESH_INVALID));
        if (!stored.equals(incoming)) {
            throw AuthException.of(AuthErrorCode.REFRESH_INVALID);
        }

        userRepository.findById(uid)
                .orElseThrow(() -> AuthException.of(AuthErrorCode.REFRESH_INVALID));

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

    // Redis에 저장된 해당 유저의 모든 refresh token 삭제
    public void logout(String uid) {
        refreshTokenRedisStore.deleteAll(uid);
    }

    // 회원탈퇴: Redis 토큰 → OAuth 레코드 → User 순으로 삭제
    @Transactional
    public void signout(String uid) {
        refreshTokenRedisStore.deleteAll(uid);
        oAuthRepository.deleteByUserUid(uid);
        userRepository.deleteById(uid);
    }

    // 네이버 앱 로그인
    @Transactional
    public NaverUserResult findOrCreateNaverAppUser(NaverUserProfile profile) {
        String providerId = profile.id();
        if (providerId == null || providerId.isBlank()) {
            throw AuthException.of(AuthErrorCode.NAVER_LOGIN_UNAUTHORIZED);
        }

        Optional<OAuth> existing = oAuthRepository.findBySocialTypeAndProviderIdWithUser(
                SocialType.NAVER, providerId);

        if (existing.isPresent()) {
            return new NaverUserResult(existing.get().getUser(), false);
        }

        User user = createNewNaverAppUser(profile);
        oAuthRepository.save(OAuth.builder()
                .user(user)
                .providerId(providerId)
                .socialType(SocialType.NAVER)
                .build());
        return new NaverUserResult(user, true);
    }

    private User createNewNaverAppUser(NaverUserProfile profile) {
        String uid  = AppUuid.newUid();
        String name = profile.name();
        return userRepository.save(User.builder()
                .uid(uid)
                .userName(name != null && !name.isBlank() ? name : null)
                .build());
    }

    // 네이버 웹(DEV) 로그인
    private User createNewDevNaverUser(NaverUserProfile profile) {
        String uid  = AppUuid.newUid();
        String name = profile.name();
        return userRepository.save(User.builder()
                .uid(uid)
                .userName(name != null && !name.isBlank() ? name : null)
                .build());
    }

    // 닉네임 형식 검증
    public boolean isValidNicknameFormat(String nickname) {
        return nickname != null && NICKNAME_PATTERN.matcher(nickname).matches();
    }

    public boolean isNicknameDuplicated(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}
