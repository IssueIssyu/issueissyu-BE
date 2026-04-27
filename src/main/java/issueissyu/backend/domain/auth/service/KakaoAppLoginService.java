package issueissyu.backend.domain.auth.service;

import issueissyu.backend.domain.auth.dto.req.KakaoAppLoginReqDTO;
import issueissyu.backend.domain.auth.dto.res.KakaoAppLoginResDTO;
import issueissyu.backend.domain.auth.exception.AuthException;
import issueissyu.backend.domain.auth.exception.code.AuthErrorCode;
import issueissyu.backend.domain.user.enums.SocialType;
import issueissyu.backend.global.redis.RefreshTokenRedisStore;
import issueissyu.backend.global.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
public class KakaoAppLoginService {

    private static final String KAKAO_USER_INFO_PATH = "/v2/user/me";
    private static final String KAKAO_PROVIDER = SocialType.KAKAO.name().toLowerCase();

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRedisStore refreshTokenRedisStore;
    private final RestClient restClient;

    public KakaoAppLoginService(
            AuthService authService,
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenRedisStore refreshTokenRedisStore,
            @Qualifier("kakaoRestClient") RestClient restClient) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenRedisStore = refreshTokenRedisStore;
        this.restClient = restClient;
    }

    public KakaoAppLoginResDTO login(KakaoAppLoginReqDTO req) {
        KakaoUserProfile profile = fetchKakaoUserProfile(req.getAccessToken());

        KakaoUserResult result = authService.findOrCreateKakaoAppUser(profile);
        String uid = result.user().getUid();
        String userName = result.user().getUserName();
        boolean isNew = result.isNew();

        String accessToken = jwtTokenProvider.createAccessToken(uid);
        String refreshToken = jwtTokenProvider.createRefreshToken(uid, KAKAO_PROVIDER);
        refreshTokenRedisStore.save(uid, KAKAO_PROVIDER, refreshToken,
                Duration.ofMillis(jwtTokenProvider.getRefreshExpMs()));

        KakaoAppLoginResDTO.UserInfo userInfo = isNew
                ? KakaoAppLoginResDTO.UserInfo.builder()
                .tempUuid(uid)
                .userName(userName)
                .build()
                : KakaoAppLoginResDTO.UserInfo.builder()
                .uuid(uid)
                .userName(userName)
                .build();

        return KakaoAppLoginResDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getAccessExpMs() / 1000)
                .isNew(isNew)
                .user(userInfo)
                .build();
    }

    @SuppressWarnings("unchecked")
    private KakaoUserProfile fetchKakaoUserProfile(String kakaoAccessToken) {
        try {
            Map<String, Object> body = restClient.get()
                    .uri(KAKAO_USER_INFO_PATH)
                    .header("Authorization", "Bearer " + kakaoAccessToken)
                    .retrieve()
                    .body(Map.class);

            if (body == null || body.get("id") == null) {
                throw AuthException.of(AuthErrorCode.KAKAO_LOGIN_UNAUTHORIZED);
            }

            Object idValue = body.get("id");
            String id = String.valueOf(idValue);

            String name = "";
            Map<String, Object> properties = (Map<String, Object>) body.get("properties");
            if (properties != null && properties.get("nickname") != null) {
                name = String.valueOf(properties.get("nickname")).trim();
            }

            if (id.isBlank() || name.isBlank()) {
                throw AuthException.of(AuthErrorCode.KAKAO_LOGIN_UNAUTHORIZED);
            }

            log.debug("카카오 앱 로그인 사용자 정보 조회 성공: id={}, nickname={}", id, name);
            return new KakaoUserProfile(id, name);
        } catch (RestClientException e) {
            log.error("카카오 사용자 정보 API 호출 실패", e);
            throw AuthException.of(AuthErrorCode.KAKAO_LOGIN_UNAUTHORIZED);
        }
    }
}
