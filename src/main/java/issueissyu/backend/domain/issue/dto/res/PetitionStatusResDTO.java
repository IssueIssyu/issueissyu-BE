package issueissyu.backend.domain.issue.dto.res;

import lombok.Builder;

@Builder
public record PetitionStatusResDTO(long pinId, int petitionCount, boolean isPetition, int targetPetition) {}
