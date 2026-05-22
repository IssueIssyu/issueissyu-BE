package issueissyu.backend.domain.location.repository;

import issueissyu.backend.domain.location.entity.PopulationDensity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PopulationDensityRepository extends JpaRepository<PopulationDensity, Long> {

    Optional<PopulationDensity> findByLocation_LocationId(Long locationId);
}