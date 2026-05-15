package issueissyu.backend.domain.user.dto.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record UserMyPinItemResDTO(
        Long pinId,
        String pinType,
        String pinDetailAddress,
        String issuePinState,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS") LocalDateTime createdAt) {}
