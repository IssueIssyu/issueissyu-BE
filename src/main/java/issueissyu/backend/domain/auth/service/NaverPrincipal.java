package issueissyu.backend.domain.auth.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class NaverPrincipal implements OAuth2User {

    private final NaverUserResult devNaverUserResult;
    private final Map<String, Object> attributes;

    public NaverPrincipal(NaverUserResult devNaverUserResult, Map<String, Object> attributes) {
        this.devNaverUserResult = devNaverUserResult;
        this.attributes = attributes;
    }

    public NaverUserResult getDevNaverUserResult() {
        return devNaverUserResult;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getName() {
        return devNaverUserResult.user().getUid();
    }
}
