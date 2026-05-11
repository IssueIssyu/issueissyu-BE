package issueissyu.backend.domain.issue.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record PetitionStatusResDTO(
        long pinId,
        int petitionCount,
        @JsonProperty("isPetitioned") boolean isPetitioned,
        int targetPetition) {}
