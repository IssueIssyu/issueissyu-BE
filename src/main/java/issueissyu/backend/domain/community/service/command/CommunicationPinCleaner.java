package issueissyu.backend.domain.community.service.command;

import issueissyu.backend.utils.S3.S3Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommunicationPinCleaner {

    private final JdbcTemplate jdbcTemplate;
    private final S3Utils s3Utils;

    @Transactional
    public void deleteByPinId(Long pinId) {
        // DB 삭제 전에 pin_image S3 key 수집
        List<String> pinImageKeys = jdbcTemplate.queryForList(
                "SELECT pin_s3_key FROM pin_image WHERE pin_id = ?",
                String.class, pinId);

        jdbcTemplate.update("DELETE FROM pin_emoji         WHERE pin_id = ?", pinId);
        jdbcTemplate.update("DELETE FROM declaration       WHERE pin_id = ?", pinId);
        jdbcTemplate.update("DELETE FROM \"comment\"       WHERE pin_id = ?", pinId);
        jdbcTemplate.update("DELETE FROM pin_like          WHERE pin_id = ?", pinId);
        jdbcTemplate.update("DELETE FROM community         WHERE pin_id = ?", pinId);
        jdbcTemplate.update("DELETE FROM pin_location      WHERE pin_id = ?", pinId);
        jdbcTemplate.update("DELETE FROM communication_pin WHERE pin_id = ?", pinId);
        jdbcTemplate.update("DELETE FROM pin_image         WHERE pin_id = ?", pinId);
        jdbcTemplate.update("DELETE FROM pin               WHERE pin_id = ?", pinId);

        // DB 삭제 완료 후 트랜잭션 커밋이 성공하면 S3 객체 삭제
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            pinImageKeys.forEach(s3Utils::deleteIfNotReserved);
                        }
                    });
        } else {
            pinImageKeys.forEach(s3Utils::deleteIfNotReserved);
        }
    }
}
