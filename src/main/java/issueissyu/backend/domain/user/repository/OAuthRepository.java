package issueissyu.backend.domain.user.repository;

import issueissyu.backend.domain.user.entity.OAuth;
import issueissyu.backend.domain.user.enums.SocialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OAuthRepository extends JpaRepository<OAuth, Long> {

    @Query("select o from OAuth o join fetch o.user where o.socialType = :socialType and o.providerId = :providerId")
    Optional<OAuth> findBySocialTypeAndProviderIdWithUser(
            @Param("socialType") SocialType socialType,
            @Param("providerId") String providerId
    );
}
