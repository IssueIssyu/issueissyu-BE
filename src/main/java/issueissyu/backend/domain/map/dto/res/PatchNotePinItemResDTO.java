package issueissyu.backend.domain.map.dto.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record PatchNotePinItemResDTO(
        Long pinId,
        String pinType,
        String pinTitle,
        int viewCount,
        String pinDetailAddress,
        String issuePinState,
        String pinUserProfile,
        String pinUserNickname,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS") LocalDateTime createdAt) {}
