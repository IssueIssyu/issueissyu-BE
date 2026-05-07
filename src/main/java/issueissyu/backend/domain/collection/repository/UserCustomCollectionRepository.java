package issueissyu.backend.domain.collection.repository;

import issueissyu.backend.domain.collection.entity.mapping.UserCustomCollection;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserCustomCollectionRepository extends JpaRepository<UserCustomCollection, Long> {

    Optional<UserCustomCollection> findByUser_UidAndCustomCollection_CustomCollectionName(
            String uid, String customCollectionName);

    @EntityGraph(attributePaths = "customCollection")
    @Query(
            """
            select ucc from UserCustomCollection ucc
            where ucc.user.uid = :uid and ucc.isProfile = true""")
    Optional<UserCustomCollection> fetchProfileMarkedForUser(@Param("uid") String uid);
}
