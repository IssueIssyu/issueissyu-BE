package issueissyu.backend.domain.map.service.query;

import issueissyu.backend.domain.map.dto.res.PatchNotePageInfoResDTO;
import issueissyu.backend.domain.map.dto.res.PatchNotePinItemResDTO;
import issueissyu.backend.domain.map.dto.res.PatchNoteResDTO;

public interface PatchNoteQueryService {

    PatchNoteResDTO getPatchNotes(String uid, String region, Integer size, String cursor);
}
