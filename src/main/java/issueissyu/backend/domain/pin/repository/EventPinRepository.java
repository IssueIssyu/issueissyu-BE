package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.EventPin;
import issueissyu.backend.domain.pin.enums.PinType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventPinRepository extends JpaRepository<EventPin, Long> {

    Optional<EventPin> findByPin_PinId(Long pinId);

    @EntityGraph(attributePaths = "storeImage")
    @Query("select ep from EventPin ep where ep.pin.pinId = :pinId")
    Optional<EventPin> findWithStoreImageByPinPinId(@Param("pinId") Long pinId);

    @Query("select ep from EventPin ep left join fetch ep.storeImage where ep.pin.pinId in :pinIds")
    List<EventPin> findWithStoreImageByPinIdIn(@Param("pinIds") Collection<Long> pinIds);

    // 알람 스케줄러용: pinType 과 eventStartTime 이 [from, to] 범위인 핀 조회
    @Query("""
            select ep from EventPin ep
            join fetch ep.pin p
            where p.pinType = :pinType
              and ep.eventStartTime between :from and :to
            """)
    List<EventPin> findAlarmTargetsByPinTypeAndStartTimeBetween(
            @Param("pinType") PinType pinType,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
