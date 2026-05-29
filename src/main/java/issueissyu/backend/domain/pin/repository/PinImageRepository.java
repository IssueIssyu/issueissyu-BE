package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.PinImage;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PinImageRepository extends JpaRepository<PinImage, Long> {

    Optional<PinImage> findFirstByPin_PinIdAndMainImageTrue(Long pinId);

    Optional<PinImage> findFirstByPin_PinIdOrderByPinImageIdAsc(Long pinId);

    List<PinImage> findByPin_PinIdOrderByPinImageIdAsc(Long pinId);

    List<PinImage> findByPin_PinIdInOrderByPin_PinIdAscMainImageDescPinImageIdAsc(
            Collection<Long> pinIds);

    Optional<PinImage> findByPin_PinIdAndPinS3Url(Long pinId, String pinS3Url);

    @Query(
            "select pi from PinImage pi join fetch pi.pin where pi.pinS3Url = :pinS3Url order by pi.pinImageId asc")
    List<PinImage> findAllWithPinByPinS3UrlOrderByPinImageIdAsc(@Param("pinS3Url") String pinS3Url);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM PinImage pi WHERE pi.pin.pinId = :pinId")
    void deleteByPin_PinId(@Param("pinId") Long pinId);
}
