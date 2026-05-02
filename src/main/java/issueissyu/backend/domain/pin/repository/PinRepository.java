package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.Pin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PinRepository extends JpaRepository<Pin, Long> {

    @Modifying(clearAutomatically = true)
    @Query(value = """
            UPDATE pin
            SET visibility_status = false
            WHERE created_at < NOW() - INTERVAL '1 year'
            """, nativeQuery = true)
    int hidePinsRegisteredOverOneYearAgo();

    @Modifying(clearAutomatically = true)
    @Query(value = """
            UPDATE pin p
            SET visibility_status = true
            FROM issue_pin ip
            WHERE ip.pin_id = p.pin_id
              AND p.pin_type = 'ISSUE'
              AND p.created_at >= NOW() - INTERVAL '1 year'
            """, nativeQuery = true)
    int updateIssuePinVisibilityBySchedule();

    @Modifying(clearAutomatically = true)
    @Query(value = """
            UPDATE pin p
            SET visibility_status =
                (p.created_at >= NOW() - INTERVAL '1 year'
                 AND cp.updated_at >= NOW() - INTERVAL '1 month')
            FROM communication_pin cp
            WHERE cp.pin_id = p.pin_id
              AND p.pin_type = 'COMMUNICATION'
            """, nativeQuery = true)
    int updateCommunicationPinVisibilityBySchedule();

    @Modifying(clearAutomatically = true)
    @Query(value = """
            UPDATE pin p
            SET visibility_status =
                (p.created_at >= NOW() - INTERVAL '1 year'
                 AND NOW() BETWEEN ep.event_start_time AND ep.event_end_time)
            FROM event_pin ep
            WHERE ep.pin_id = p.pin_id
              AND p.pin_type = 'FESTIVAL'
            """, nativeQuery = true)
    int updateFestivalPinVisibilityBySchedule();
}