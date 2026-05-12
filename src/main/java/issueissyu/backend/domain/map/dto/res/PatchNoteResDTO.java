package issueissyu.backend.domain.map.dto.res;

import java.util.List;

public record PatchNoteResDTO(List<PatchNotePinItemResDTO> pins, PatchNotePageInfoResDTO pageInfo) {}
