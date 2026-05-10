package issueissyu.backend.domain.community.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommunicationPinCleaner {

    private final JdbcTemplate jdbcTemplate;

    public void deleteByPinId(Long pinId) {
        jdbcTemplate.update("DELETE FROM pin_emoji     WHERE pin_id = ?", pinId);
        jdbcTemplate.update("DELETE FROM declaration   WHERE pin_id = ?", pinId);
        jdbcTemplate.update("DELETE FROM \"comment\"   WHERE pin_id = ?", pinId);
        jdbcTemplate.update("DELETE FROM pin_like      WHERE pin_id = ?", pinId);
        jdbcTemplate.update("DELETE FROM community     WHERE pin_id = ?", pinId);
        jdbcTemplate.update("DELETE FROM pin_location  WHERE pin_id = ?", pinId);
        jdbcTemplate.update("DELETE FROM communication_pin WHERE pin_id = ?", pinId);
        jdbcTemplate.update("DELETE FROM pin_image     WHERE pin_id = ?", pinId);
        jdbcTemplate.update("DELETE FROM pin           WHERE pin_id = ?", pinId);
    }
}
