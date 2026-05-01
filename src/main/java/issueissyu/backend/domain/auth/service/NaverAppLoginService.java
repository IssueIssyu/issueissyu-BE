package issueissyu.backend.domain.auth.service;

import issueissyu.backend.domain.auth.dto.req.NaverAppLoginReqDTO;
import issueissyu.backend.domain.auth.dto.res.NaverAppLoginResDTO;
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

// 네이버 앱 로그인 서비스
// 앱(안드로이드)에서 네이버 SDK로 발급받은 access_token을 받아 네이버 사용자 정보 API를 호출하고,
// 자체 JWT를 발급하여 반환한다. (인가 코드 발급은 앱이 수행, 토큰 교환 이후 처리는 백엔드가 수행)
@Slf4j
@Service
public class NaverAppLoginService {

    private static final String NAVER_USER_INFO_PATH = "/v1/nid/me";
    private static final String NAVER_PROVIDER = SocialType.NAVER.name().toLowerCase();

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRedisStore refreshTokenRedisStore;
    private final RestClient restClient;

    public NaverAppLoginService(
            AuthService authService,
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenRedisStore refreshTokenRedisStore,
            @Qualifier("naverRestClient") RestClient restClient) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenRedisStore = refreshTokenRedisStore;
        this.restClient = restClient;
    }

    public NaverAppLoginResDTO login(NaverAppLoginReqDTO req) {
        // 네이버 사용자 정보 API 호출 (앱이 발급받은 access_token 사용)
        NaverUserProfile profile = fetchNaverUserProfile(req.getAccessToken());

        // DB 조회 또는 신규 사용자 생성
        NaverUserResult result = authService.findOrCreateNaverAppUser(profile);
        String uid      = result.user().getUid();
        String userName = result.user().getUserName();
        boolean isNew   = result.isNew();

        // 자체 JWT 발급 + Redis에 refresh token 저장
        String accessToken  = jwtTokenProvider.createAccessToken(uid);
        String refreshToken = jwtTokenProvider.createRefreshToken(uid, NAVER_PROVIDER);
        refreshTokenRedisStore.save(uid, NAVER_PROVIDER, refreshToken,
                Duration.ofMillis(jwtTokenProvider.getRefreshExpMs()));

        // 응답 조립
        // 신규 회원: temp_uuid (온보딩 완료 전), 기존 회원: uuid
        NaverAppLoginResDTO.UserInfo userInfo = isNew
                ? NaverAppLoginResDTO.UserInfo.builder()
                        .tempUuid(uid)
                        .userName(userName)
                        .build()
                : NaverAppLoginResDTO.UserInfo.builder()
                        .uuid(uid)
                        .userName(userName)
                        .build();

        return NaverAppLoginResDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getAccessExpMs() / 1000)
                .isNew(isNew)
                .socialType(SocialType.NAVER.name())
                .user(userInfo)
                .build();
    }

    @SuppressWarnings("unchecked")
    private NaverUserProfile fetchNaverUserProfile(String naverAccessToken) {
        try {
            Map<String, Object> body = restClient.get()
                    .uri(NAVER_USER_INFO_PATH)
                    .header("Authorization", "Bearer " + naverAccessToken)
                    .retrieve()
                    .body(Map.class);

            if (body == null) {
                throw AuthException.of(AuthErrorCode.NAVER_API_FAILED);
            }

            String resultCode = (String) body.get("resultcode");
            if (!"00".equals(resultCode)) {
                log.warn("네이버 사용자 정보 API 오류 응답: resultcode={}, message={}",
                        resultCode, body.get("message"));
                throw AuthException.of(AuthErrorCode.NAVER_LOGIN_UNAUTHORIZED);
            }

            Map<String, Object> response = (Map<String, Object>) body.get("response");
            String id   = (String) response.get("id");
            String name = (String) response.getOrDefault("name", "");

            if (id == null || id.isBlank()) {
                throw AuthException.of(AuthErrorCode.NAVER_LOGIN_UNAUTHORIZED);
            }

            log.debug("네이버 앱 로그인 사용자 정보 조회 성공: id={}, name={}", id, name);
            return new NaverUserProfile(id, name);

        } catch (RestClientException e) {
            log.error("네이버 사용자 정보 API 호출 실패", e);
            throw AuthException.of(AuthErrorCode.NAVER_API_FAILED);
        }
    }
}
