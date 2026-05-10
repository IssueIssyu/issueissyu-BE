package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.PinImage;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PinImageRepository extends JpaRepository<PinImage, Long> {

    Optional<PinImage> findFirstByPin_PinIdAndMainImageTrue(Long pinId);

    Optional<PinImage> findFirstByPin_PinIdOrderByPinImageIdAsc(Long pinId);

    List<PinImage> findByPin_PinIdOrderByPinImageIdAsc(Long pinId);

    List<PinImage> findByPin_PinIdInOrderByPin_PinIdAscMainImageDescPinImageIdAsc(
            Collection<Long> pinIds);
}
