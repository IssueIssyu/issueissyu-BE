package issueissyu.backend.domain.user.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

// Redis에 저장되는 리프레시 토큰.
// 키 형식: token_redis:{uid}:{provider}  (예: token_redis:abc123:naver)
// 소셜 로그인 제공자별로 분리 관리
// TTL 만료 시 Redis가 자동 삭제
@RedisHash("token_redis")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    private String id; // "uid:provider" 복합 키

    private String refreshToken;

    @TimeToLive
    private Long expiration; // 초 단위 TTL

    public static RefreshToken create(String uid, String provider, String token, long ttlSeconds) {
        RefreshToken rt = new RefreshToken();
        rt.id = uid + ":" + provider;
        rt.refreshToken = token;
        rt.expiration = ttlSeconds;
        return rt;
    }
}
