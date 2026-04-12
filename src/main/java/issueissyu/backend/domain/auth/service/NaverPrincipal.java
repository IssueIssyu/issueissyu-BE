package issueissyu.backend.domain.auth.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class NaverPrincipal implements OAuth2User {

    private final NaverUserResult navResult;
    private final Map<String, Object> attributes;

    public NaverPrincipal(NaverUserResult navResult, Map<String, Object> attributes) {
        this.navResult = navResult;
        this.attributes = attributes;
    }

    public NaverUserResult getNavResult() {
        return navResult;
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
        return navResult.user().getUid();
    }
}
