package issueissyu.backend.domain.collection.repository;

import issueissyu.backend.domain.collection.entity.mapping.UserCustomCollection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCustomCollectionRepository extends JpaRepository<UserCustomCollection, Long> {

    Optional<UserCustomCollection> findByUser_UidAndCustomCollection_CustomCollectionName(
            String uid, String customCollectionName);
}
