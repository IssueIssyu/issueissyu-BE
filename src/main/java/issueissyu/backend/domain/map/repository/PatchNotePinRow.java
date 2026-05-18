package issueissyu.backend.domain.map.repository;

import java.time.LocalDateTime;

// 네이티브 조회 결과 매핑 (복합 정렬·커서와 일치하는 컬럼 별칭)
public interface PatchNotePinRow {
    Long getPinId();

    String getPinType();

    String getPinTitle();

    Integer getViewCount();

    String getPinDetailAddress();

    String getIssuePinState();

    String getPinUserProfile();

    String getPinUserNickname();

    LocalDateTime getCreatedAt();
}
