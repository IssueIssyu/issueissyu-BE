package issueissyu.backend.domain.pin.service.command;

import issueissyu.backend.domain.pin.dto.req.ApplyPinEmojiReqDTO;
import issueissyu.backend.domain.pin.dto.res.ApplyPinEmojiResDTO;

public interface PinEmojiCommandService {
    ApplyPinEmojiResDTO applyMyEmoji(Long pinId, String uid, ApplyPinEmojiReqDTO request);
}
