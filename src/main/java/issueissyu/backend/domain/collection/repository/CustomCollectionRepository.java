package issueissyu.backend.domain.collection.repository;

import issueissyu.backend.domain.collection.entity.CustomCollection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomCollectionRepository extends JpaRepository<CustomCollection, Long> {

    Optional<CustomCollection> findByCustomCollectionName(String customCollectionName);

    List<CustomCollection> findAllByOrderByCustomCollectionIdAsc();
}
