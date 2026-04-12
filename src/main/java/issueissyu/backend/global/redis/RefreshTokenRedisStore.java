package issueissyu.backend.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshTokenRedisStore {

    private static final String KEY_PREFIX = "issueissyu:auth:refresh:";

    private final StringRedisTemplate stringRedisTemplate;

    public void save(String uid, String refreshToken, Duration ttl) {
        stringRedisTemplate.opsForValue().set(KEY_PREFIX + uid, refreshToken, ttl);
    }

    public Optional<String> find(String uid) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(KEY_PREFIX + uid));
    }

    public void delete(String uid) {
        stringRedisTemplate.delete(KEY_PREFIX + uid);
    }
}
