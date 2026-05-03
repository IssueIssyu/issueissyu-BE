package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.Pin;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PinRepository extends JpaRepository<Pin, Long> {

    @EntityGraph(attributePaths = "user")
    @Query("select p from Pin p where p.pinId = :id")
    Optional<Pin> fetchDetailWithAuthor(@Param("id") Long pinId);
}
