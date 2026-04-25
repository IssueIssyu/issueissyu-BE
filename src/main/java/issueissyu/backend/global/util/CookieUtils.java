package issueissyu.backend.global.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CookieUtils {

    private final ObjectMapper objectMapper;

    public Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .findFirst();
    }

    public void addCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAgeSeconds);
        response.addCookie(cookie);
    }

    public void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        if (request.getCookies() == null) return;
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }

    public String serialize(Object object) {
        try {
            ObjectMapper mapper = objectMapper.copy();
            mapper.registerModules(SecurityJackson2Modules.getModules(CookieUtils.class.getClassLoader()));
            byte[] jsonBytes = mapper.writeValueAsBytes(object);
            return Base64.getUrlEncoder().encodeToString(jsonBytes);
        } catch (Exception e) {
            throw new IllegalStateException("쿠키 직렬화 실패", e);
        }
    }

    public <T> T deserialize(Cookie cookie, Class<T> cls) {
        try {
            ObjectMapper mapper = objectMapper.copy();
            mapper.registerModules(SecurityJackson2Modules.getModules(CookieUtils.class.getClassLoader()));
            byte[] jsonBytes = Base64.getUrlDecoder().decode(cookie.getValue());
            return mapper.readValue(jsonBytes, cls);
        } catch (Exception e) {
            throw new IllegalStateException("쿠키 역직렬화 실패", e);
        }
    }
}
