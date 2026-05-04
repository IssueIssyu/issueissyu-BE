package issueissyu.backend.domain.location.repository;

import issueissyu.backend.domain.location.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {

    /**
     * {@code location} 컬럼(법정동코드 문자열) 앞 5자리가 시군구와 일치하는 행을 조회합니다.
     *
     * @param prefix 법정동코드 앞 5자리 (예: 네이버 {@code legalDistrictCode}의 앞 5글자)
     */
    @Query("SELECT l FROM Location l WHERE SUBSTRING(l.region, 1, 5) = :prefix")
    List<Location> findAllByLocationSigunguPrefix(@Param("prefix") String prefix);
}
