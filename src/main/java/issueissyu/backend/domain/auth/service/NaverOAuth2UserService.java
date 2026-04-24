package issueissyu.backend.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverOAuth2UserService extends DefaultOAuth2UserService {

    private final AuthService authService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        @SuppressWarnings("unchecked")
        Map<String, Object> devNaverResponse = (Map<String, Object>) attributes.get("response");

        String devNaverId = (String) devNaverResponse.get("id");
        String devNaverName = (String) devNaverResponse.getOrDefault("name", "");

        log.debug("DevNaverOAuth2 profile: id={}, name={}", devNaverId, devNaverName);

        NaverUserProfile profile = new NaverUserProfile(devNaverId, devNaverName);
        NaverUserResult devNaverUserResult = authService.findOrCreateDevNaverUser(profile);

        return new NaverPrincipal(devNaverUserResult, attributes);
    }
}
