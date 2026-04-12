package issueissyu.backend.global.security;

import issueissyu.backend.global.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

// OAuth2 인가 요청(state 등)과 로그인 성공 후 리다이렉트할 URI를 쿠키에 보관.
// - oauth2_auth_request : 직렬화된 OAuth2AuthorizationRequest
//- redirect_uri        : 프론트가 /oauth2/authorization/naver?redirect_uri=... 로 전달한 값
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTH_REQUEST_COOKIE  = "oauth2_auth_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return CookieUtils.getCookie(request, OAUTH2_AUTH_REQUEST_COOKIE)
                .map(c -> {
                    try {
                        return CookieUtils.deserialize(c, OAuth2AuthorizationRequest.class);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response);
            return;
        }

        CookieUtils.addCookie(response, OAUTH2_AUTH_REQUEST_COOKIE,
                CookieUtils.serialize(authorizationRequest), COOKIE_EXPIRE_SECONDS);

        // 프론트가 전달한 redirect_uri 쿠키로 보관 (있을 때만)
        String redirectUri = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        if (redirectUri != null && !redirectUri.isBlank()) {
            CookieUtils.addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME,
                    redirectUri, COOKIE_EXPIRE_SECONDS);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                  HttpServletResponse response) {
        OAuth2AuthorizationRequest req = loadAuthorizationRequest(request);
        CookieUtils.deleteCookie(request, response, OAUTH2_AUTH_REQUEST_COOKIE);
        return req;
    }

    // 인증 완료 후 모든 OAuth2 관련 쿠키 정리
    public void removeAuthorizationRequestCookies(HttpServletRequest request,
                                                   HttpServletResponse response) {
        CookieUtils.deleteCookie(request, response, OAUTH2_AUTH_REQUEST_COOKIE);
        CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
    }
}
