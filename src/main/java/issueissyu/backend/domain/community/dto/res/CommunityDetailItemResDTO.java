package issueissyu.backend.domain.community.dto.res;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kind")
@JsonSubTypes({
        @JsonSubTypes.Type(value = IssueCommunityDetailItemResDTO.class, name = "ISSUE"),
        @JsonSubTypes.Type(value = StoreCommunityFeedItemResDTO.class, name = "STORE"),
        @JsonSubTypes.Type(value = CommunicationCommunityFeedItemResDTO.class, name = "COMMUNICATION")
})
public interface CommunityDetailItemResDTO {
}
