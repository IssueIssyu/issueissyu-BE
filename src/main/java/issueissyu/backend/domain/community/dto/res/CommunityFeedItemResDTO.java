package issueissyu.backend.domain.community.dto.res;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kind")
@JsonSubTypes({
        @JsonSubTypes.Type(value = IssueCommunityFeedItemResDTO.class, name = "ISSUE"),
        @JsonSubTypes.Type(value = StoreCommunityFeedItemResDTO.class, name = "STORE"),
        @JsonSubTypes.Type(value = FestivalCommunityFeedItemResDTO.class, name = "FESTIVAL"),
        @JsonSubTypes.Type(value = CommunicationCommunityFeedItemResDTO.class, name = "COMMUNICATION"),
        @JsonSubTypes.Type(value = PolicyCommunityFeedItemResDTO.class, name = "POLICY"),
        @JsonSubTypes.Type(value = ContestCommunityFeedItemResDTO.class, name = "CONTEST"),
        @JsonSubTypes.Type(value = CardnewsCommunityFeedItemResDTO.class, name = "CARDNEWS")
})
public sealed interface CommunityFeedItemResDTO
        permits IssueCommunityFeedItemResDTO,
        StoreCommunityFeedItemResDTO,
        FestivalCommunityFeedItemResDTO,
        CommunicationCommunityFeedItemResDTO,
        PolicyCommunityFeedItemResDTO,
        ContestCommunityFeedItemResDTO,
        CardnewsCommunityFeedItemResDTO{
}
