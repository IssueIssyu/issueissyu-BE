package issueissyu.backend.domain.user.repository;

import java.time.LocalDateTime;

public interface UserMyPinRow {
    Long getPinId();

    String getPinType();

    String getPinTitle();

    String getPinDetailAddress();

    String getIssuePinState();

    LocalDateTime getCreatedAt();
}
