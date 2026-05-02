-- 지도 핀 노출: visibility_status 제거, 소통 핀 마지막 반응 시각 비정규화, 조회 쿼리용 인덱스

ALTER TABLE communication_pin
    ADD COLUMN IF NOT EXISTS last_activity_at TIMESTAMP;

UPDATE communication_pin cp
SET last_activity_at = sub.calculated_at
FROM (
    SELECT cp2.pin_id AS pin_id,
           GREATEST(
               cp2.created_at,
               COALESCE((SELECT MAX(c.created_at) FROM "comment" c WHERE c.pin_id = cp2.pin_id),
                        cp2.created_at),
               COALESCE((SELECT MAX(GREATEST(pe.created_at, pe.updated_at))
                         FROM pin_emoji pe
                         WHERE pe.pin_id = cp2.pin_id),
                        cp2.created_at)
           ) AS calculated_at
    FROM communication_pin cp2
) sub
WHERE cp.pin_id = sub.pin_id
  AND cp.last_activity_at IS NULL;

UPDATE communication_pin
SET last_activity_at = created_at
WHERE last_activity_at IS NULL;

ALTER TABLE communication_pin
    ALTER COLUMN last_activity_at SET NOT NULL;

ALTER TABLE pin
    DROP COLUMN IF EXISTS visibility_status;

CREATE INDEX IF NOT EXISTS idx_pin_created_at ON pin (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_communication_pin_last_activity_at ON communication_pin (last_activity_at DESC);
CREATE INDEX IF NOT EXISTS idx_event_pin_event_window ON event_pin (event_start_time, event_end_time);
