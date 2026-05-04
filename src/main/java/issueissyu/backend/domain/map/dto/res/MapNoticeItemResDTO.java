package issueissyu.backend.domain.map.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record MapNoticeItemResDTO(
        @JsonProperty("notice_id") long noticeId, long pinId, String noticeContent) {}
