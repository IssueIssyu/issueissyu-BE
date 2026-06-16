package issueissyu.backend.domain.user.repository;

import java.time.LocalDateTime;

public interface UserMySolverRow {
    Long getPinId();

    String getPinTitle();

    String getPinDetailAddress();

    String getIssuePinState();

    LocalDateTime getCreatedAt();

    Long getProblemSolverId();
}
