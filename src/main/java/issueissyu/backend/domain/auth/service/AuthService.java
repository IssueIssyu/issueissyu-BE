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
import issueissyu.backend.global.api.code.GeneralErrorCode;
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
        if (!jwtTokenProvider.validateToken(incoming)) {
            throw GeneralException.of(GeneralErrorCode.TOKEN_INVALID);
        }
        if (!JwtTokenProvider.TOKEN_TYPE_REFRESH.equals(jwtTokenProvider.parseTokenType(incoming))) {
            throw GeneralException.of(GeneralErrorCode.TOKEN_INVALID);
        }

        String uid = jwtTokenProvider.parseUid(incoming);
        String stored = refreshTokenRedisStore.find(uid)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.WRONG_REFRESH_TOKEN));
        if (!stored.equals(incoming)) {
            throw GeneralException.of(GeneralErrorCode.WRONG_REFRESH_TOKEN);
        }

        userRepository.findById(uid)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        String newAccess   = jwtTokenProvider.createAccessToken(uid);
        String newRefresh  = jwtTokenProvider.createRefreshToken(uid);
        refreshTokenRedisStore.save(uid, newRefresh, Duration.ofMillis(jwtTokenProvider.getRefreshExpMs()));

        return TokenPairDTO.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .expiresIn(jwtTokenProvider.getAccessExpMs())
                .build();
    }

    public void logout(String uid) {
        refreshTokenRedisStore.delete(uid);
    }

    private User createNewNaverUser(NaverUserProfile profile) {
        String uid  = AppUuid.newUid();
        String name = profile.name();
        return userRepository.save(User.builder()
                .uid(uid)
                .nickname(name != null && !name.isBlank() ? name : null)
                .build());
    }
}
