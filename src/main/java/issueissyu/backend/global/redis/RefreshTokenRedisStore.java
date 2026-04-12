package issueissyu.backend.global.redis;

import issueissyu.backend.domain.user.entity.RefreshToken;
import issueissyu.backend.domain.user.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

// Redis 리프레시 토큰 저장소.
// 키 형식: token_redis:{uid}:{provider}
@Component
@RequiredArgsConstructor
public class RefreshTokenRedisStore {

    private static final String KEY_PREFIX = "token_redis:";

    private final RefreshTokenRepository refreshTokenRepository;
    private final StringRedisTemplate stringRedisTemplate;

    public void save(String uid, String provider, String token, Duration ttl) {
        refreshTokenRepository.save(
                RefreshToken.create(uid, provider, token, ttl.toSeconds()));
    }

    public Optional<String> find(String uid, String provider) {
        return refreshTokenRepository.findById(uid + ":" + provider)
                .map(RefreshToken::getRefreshToken);
    }

    public void delete(String uid, String provider) {
        refreshTokenRepository.deleteById(uid + ":" + provider);
    }

    // 특정 유저의 모든 소셜 토큰 삭제 (전체 로그아웃·회원탈퇴)
    public void deleteAll(String uid) {
        Set<String> keys = stringRedisTemplate.keys(KEY_PREFIX + uid + ":*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }
}
