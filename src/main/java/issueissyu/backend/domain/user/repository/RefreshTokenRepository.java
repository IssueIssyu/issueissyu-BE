package issueissyu.backend.domain.user.repository;

import issueissyu.backend.domain.user.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
