package issueissyu.backend.domain.collection.repository;

import issueissyu.backend.domain.collection.entity.mapping.UserCustomCollection;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserCustomCollectionRepository extends JpaRepository<UserCustomCollection, Long> {

    Optional<UserCustomCollection> findByUser_UidAndCustomCollection_CustomCollectionName(
            String uid, String customCollectionName);

    @EntityGraph(attributePaths = {"customCollection", "user"})
    @Query("select ucc from UserCustomCollection ucc where ucc.isProfile = true and ucc.user.uid in :uids")
    List<UserCustomCollection> findProfilesByUserUidIn(@Param("uids") Collection<String> uids);

    @EntityGraph(attributePaths = "customCollection")
    @Query(
            """
            select ucc from UserCustomCollection ucc
            where ucc.user.uid = :uid and ucc.isProfile = true""")
    Optional<UserCustomCollection> fetchProfileMarkedForUser(@Param("uid") String uid);

    @EntityGraph(attributePaths = "customCollection")
    List<UserCustomCollection> findAllByUser_UidOrderByCustomCollection_CustomCollectionIdAsc(String uid);

    @EntityGraph(attributePaths = "customCollection")
    Optional<UserCustomCollection> findByUser_UidAndCustomCollection_CustomCollectionId(
            String uid, Long customCollectionId);
}
