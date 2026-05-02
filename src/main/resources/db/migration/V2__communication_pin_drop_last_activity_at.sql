-- last_activity_at 을 지도 노출용 시각으로 쓰던 값을 updated_at 으로 통합

UPDATE communication_pin
SET updated_at = GREATEST(
        COALESCE(updated_at, created_at),
        COALESCE(last_activity_at, created_at)
    );

ALTER TABLE communication_pin DROP COLUMN IF EXISTS last_activity_at;

DROP INDEX IF EXISTS idx_communication_pin_last_activity_at;
CREATE INDEX IF NOT EXISTS idx_communication_pin_updated_at ON communication_pin (updated_at DESC);
