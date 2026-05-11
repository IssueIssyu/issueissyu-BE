package issueissyu.backend.domain.map.repository;

import issueissyu.backend.domain.pin.entity.Pin;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface PatchNoteRepository extends Repository<Pin, Long> {

    // 패치노트 조회 (쿼리샤갈이게맞나)
    @Query(
            value =
                    """
                    SELECT
                        p.pin_id AS "pinId",
                        p.pin_type AS "pinType",
                        pl.detail_address AS "pinDetailAddress",
                        ip.issue_pin_state AS "issuePinState",
                        p.created_at AS "createdAt"
                    FROM pin p
                    INNER JOIN issue_pin ip ON ip.pin_id = p.pin_id
                    INNER JOIN (
                        SELECT DISTINCT ON (pl.pin_id) pl.pin_id, pl.detail_address
                        FROM pin_location pl
                        INNER JOIN location loc_r
                            ON pl.location_id = loc_r.location_id
                            AND loc_r.location = :region
                        ORDER BY pl.pin_id, pl.pin_location_id ASC
                    ) pl ON pl.pin_id = p.pin_id
                    WHERE p.pin_type = 'ISSUE'
                      AND (
                        NOT CAST(:applyCursor AS boolean)
                        OR ROW(
                            CASE ip.issue_pin_state
                                WHEN 'BEFORE_PROGRESS' THEN 0
                                WHEN 'IN_PROGRESS' THEN 1
                                ELSE 2
                            END,
                            p.created_at,
                            p.pin_id
                        ) > ROW(
                            :cursorRank,
                            CAST(:cursorCreatedAt AS timestamp),
                            :cursorPinId
                        )
                      )
                    ORDER BY
                        CASE ip.issue_pin_state
                            WHEN 'BEFORE_PROGRESS' THEN 0
                            WHEN 'IN_PROGRESS' THEN 1
                            ELSE 2
                        END ASC,
                        p.created_at ASC,
                        p.pin_id ASC
                    LIMIT :limit
                    """,
            nativeQuery = true)
    List<PatchNotePinRow> findPatchNotes(
            @Param("region") String region,
            @Param("applyCursor") boolean applyCursor,
            @Param("cursorRank") int cursorRank,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorPinId") long cursorPinId,
            @Param("limit") int limit);
}
