package issueissyu.backend.domain.pin.service.query;

import issueissyu.backend.domain.pin.dto.res.EmojiCandidateResDTO;
import issueissyu.backend.domain.pin.dto.res.PinEmojiSummaryListResDTO;

import java.util.List;

public interface PinEmojiQueryService {
    PinEmojiSummaryListResDTO getPinEmojiSummaries(Long pinId, String uid);

    List<EmojiCandidateResDTO> getEmojiCandidates(String uid);
}
