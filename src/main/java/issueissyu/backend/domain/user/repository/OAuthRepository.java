package issueissyu.backend.domain.user.repository;

import issueissyu.backend.domain.user.entity.OAuth;
import issueissyu.backend.domain.user.enums.SocialType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OAuthRepository extends JpaRepository<OAuth, Long> {

    @EntityGraph(attributePaths = {"user", "user.oauths"})
    @Query("select o from OAuth o where o.socialType = :socialType and o.providerId = :providerId")
    Optional<OAuth> findBySocialTypeAndProviderIdWithUser(
            @Param("socialType") SocialType socialType,
            @Param("providerId") String providerId
    );

    @Modifying
    @Query("delete from OAuth o where o.user.uid = :uid")
    void deleteByUserUid(@Param("uid") String uid);

    boolean existsByUser_UidAndSocialType(String uid, SocialType socialType);
    Optional<OAuth> findFirstByUser_Uid(String uid);
    Optional<OAuth> findByUser_UidAndSocialType(String uid, SocialType socialType);
    boolean existsByProviderIdAndSocialType(String providerId, SocialType socialType);
}
