package issueissyu.backend.domain.pin.service.command;

import issueissyu.backend.domain.pin.dto.req.CommunicationPinEditReqDTO;
import issueissyu.backend.domain.pin.dto.req.CommunicationPinImportReqDTO;
import issueissyu.backend.domain.pin.dto.req.CommunicationPinImportMultipartReqDTO;
import issueissyu.backend.domain.pin.dto.res.CommunicationPinEditResDTO;
import issueissyu.backend.domain.pin.dto.res.CommunicationPinImportResDTO;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface PinCommunicationCommandService {

    CommunicationPinImportResDTO importCommunication(String uid, CommunicationPinImportReqDTO request);

    CommunicationPinImportResDTO importCommunicationV1(
            String uid, CommunicationPinImportMultipartReqDTO request, List<MultipartFile> photos);

    CommunicationPinEditResDTO editCommunication(String uid, Long pinId, CommunicationPinEditReqDTO request);
}
