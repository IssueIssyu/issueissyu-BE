package issueissyu.backend.domain.issue.dto.res;

import lombok.Builder;

@Builder
public record PetitionSubmitResDTO(long pinId, int petitionCount, boolean isPetition) {}
