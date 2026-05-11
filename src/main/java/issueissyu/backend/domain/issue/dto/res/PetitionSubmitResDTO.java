package issueissyu.backend.domain.issue.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record PetitionSubmitResDTO(
        long pinId, int petitionCount, @JsonProperty("isPetitioned") boolean isPetitioned) {}
