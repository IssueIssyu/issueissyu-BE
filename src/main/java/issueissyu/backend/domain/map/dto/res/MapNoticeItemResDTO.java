package issueissyu.backend.domain.map.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record MapNoticeItemResDTO(
        @JsonProperty("notice_id") long noticeId,
        @JsonProperty("pin_id") long pinId,
        @JsonProperty("notice_content") String noticeContent
) {}