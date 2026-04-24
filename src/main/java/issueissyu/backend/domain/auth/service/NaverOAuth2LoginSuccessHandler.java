package issueissyu.backend.domain.auth.service;

import issueissyu.backend.domain.user.enums.SocialType;
import issueissyu.backend.global.redis.RefreshTokenRedisStore;
import issueissyu.backend.global.security.HttpCookieOAuth2AuthorizationRequestRepository;
import issueissyu.backend.global.security.JwtTokenProvider;
import issueissyu.backend.global.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;

import static issueissyu.backend.global.security.HttpCookieOAuth2AuthorizationRequestRepository.DEV_REDIRECT_URI_PARAM_COOKIE_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverOAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String DEV_NAVER_PROVIDER = SocialType.NAVER.name().toLowerCase();

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRedisStore refreshTokenRedisStore;
    private final HttpCookieOAuth2AuthorizationRequestRepository devAuthRequestRepository;

    @Value("${app.dev-oauth2.redirect-uri:http://localhost:8080/dev/login/callback}")
    private String devDefaultRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        devAuthRequestRepository.removeDevAuthorizationRequestCookies(request, response);

        try {
            NaverPrincipal devNaverPrincipal = (NaverPrincipal) authentication.getPrincipal();
            NaverUserResult devNaverUserResult = devNaverPrincipal.getDevNaverUserResult();
            String uid = devNaverUserResult.user().getUid();

            // JWT 발급 + Redis 저장 (key: token_redis:{uid}:naver)
            String accessToken  = jwtTokenProvider.createAccessToken(uid);
            String refreshToken = jwtTokenProvider.createRefreshToken(uid, DEV_NAVER_PROVIDER);
            refreshTokenRedisStore.save(uid, DEV_NAVER_PROVIDER, refreshToken,
                    Duration.ofMillis(jwtTokenProvider.getRefreshExpMs()));

            // accessToken + refreshToken → HttpOnly 쿠키
            CookieUtils.addCookie(response, "accessToken", accessToken,
                    (int) (jwtTokenProvider.getAccessExpMs() / 1000));
            CookieUtils.addCookie(response, "refreshToken", refreshToken,
                    (int) (jwtTokenProvider.getRefreshExpMs() / 1000)); // 14일

            // Dev OAuth2 시작 시 전달된 dev_redirect_uri 쿠키 우선 사용, 없으면 기본값
            String targetBaseUrl = CookieUtils.getCookie(request, DEV_REDIRECT_URI_PARAM_COOKIE_NAME)
                    .map(Cookie::getValue)
                    .orElse(devDefaultRedirectUri);

            // 민감하지 않은 devIsNew 플래그만 URL 파라미터로 전달
            String targetUrl = UriComponentsBuilder.fromUriString(targetBaseUrl)
                    .queryParam("devIsNew", devNaverUserResult.isNew())
                    .build().toUriString();

            log.info("OAuth2 로그인 성공 → redirect: {}", targetUrl);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (Exception e) {
            log.error("OAuth2 로그인 성공 후 처리 중 오류 발생", e);
            if (!response.isCommitted()) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write(
                        "{\"isSuccess\":false,\"code\":\"LOGIN_PROCESS_ERROR\",\"message\":\""
                                + e.getClass().getSimpleName() + ": " + e.getMessage() + "\"}");
            }
        }
    }
}
