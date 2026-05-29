package issueissyu.backend.domain.user.repository;

import issueissyu.backend.domain.pin.entity.Pin;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserMyPinRepository extends JpaRepository<Pin, Long> {

    @Query(
            value =
                    """
                    SELECT p.pin_id AS pinId,
                           p.pin_type AS pinType,
                           p.pin_title AS pinTitle,
                           pl.detail_address AS pinDetailAddress,
                           ip.issue_pin_state AS issuePinState,
                           p.created_at AS createdAt
                    FROM pin p
                    LEFT JOIN issue_pin ip ON ip.pin_id = p.pin_id
                    LEFT JOIN LATERAL (
                        SELECT pl2.detail_address
                        FROM pin_location pl2
                        WHERE pl2.pin_id = p.pin_id
                        ORDER BY pl2.pin_location_id ASC
                        LIMIT 1
                    ) pl ON TRUE
                    WHERE p.uid = :uid
                      AND (
                          NOT CAST(:applyCursor AS boolean)
                          OR p.created_at < CAST(:cursorCreatedAt AS timestamp)
                          OR (
                              p.created_at = CAST(:cursorCreatedAt AS timestamp)
                              AND p.pin_id < :cursorPinId
                          )
                      )
                    ORDER BY p.created_at DESC, p.pin_id DESC
                    LIMIT :limit
                    """,
            nativeQuery = true)
    List<UserMyPinRow> findMyPins(
            @Param("uid") String uid,
            @Param("applyCursor") boolean applyCursor,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorPinId") long cursorPinId,
            @Param("limit") int limit);
}
