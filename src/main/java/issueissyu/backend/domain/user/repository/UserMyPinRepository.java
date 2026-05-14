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
                           pl.detail_address AS pinDetailAddress,
                           ip.issue_pin_state AS issuePinState,
                           p.created_at AS createdAt
                    FROM pin p
                    LEFT JOIN issue_pin ip ON ip.pin_id = p.pin_id
                    LEFT JOIN (
                        SELECT DISTINCT ON (pl2.pin_id) pl2.pin_id, pl2.detail_address
                        FROM pin_location pl2
                        ORDER BY pl2.pin_id, pl2.pin_location_id ASC
                    ) pl ON pl.pin_id = p.pin_id
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
