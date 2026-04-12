package issueissyu.backend.domain.user.repository;

import issueissyu.backend.domain.user.entity.TokenRedis;
import org.springframework.data.jpa.repository.JpaRepository;

// RDB token_redis 엔터티용. 실제 리프레시 세션은 {@link issueissyu.backend.global.redis.RefreshTokenRedisStore} 사용.
public interface TokenRedisRepository extends JpaRepository<TokenRedis, Long> {
}
