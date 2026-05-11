package issueissyu.backend.domain.pin.service.command;

import issueissyu.backend.domain.pin.dto.req.CommunicationPinEditReqDTO;
import issueissyu.backend.domain.pin.dto.req.CommunicationPinImportReqDTO;
import issueissyu.backend.domain.pin.dto.res.CommunicationPinEditResDTO;
import issueissyu.backend.domain.pin.dto.res.CommunicationPinImportResDTO;

public interface PinCommunicationCommandService {

    CommunicationPinImportResDTO importCommunication(String uid, CommunicationPinImportReqDTO request);

    CommunicationPinEditResDTO editCommunication(String uid, Long pinId, CommunicationPinEditReqDTO request);
}
