package issueissyu.backend.domain.location.repository;

import issueissyu.backend.domain.location.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    /**
     * {@code adm_code} 컬럼에서 시군구 코드(앞 5자리) + {@code "00000"} 으로 구성된 정확한 코드와 일치하는 행을 조회합니다.
     *
     * @param sigunguCode 법정동코드 앞 5자리 + "00000" (예: "1168000000")
     */
    Optional<Location> findByAdmCode(String sigunguCode);
}
