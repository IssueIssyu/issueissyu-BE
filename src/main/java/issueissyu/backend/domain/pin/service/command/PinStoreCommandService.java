package issueissyu.backend.domain.pin.service.command;

import issueissyu.backend.domain.pin.dto.req.StorePinImportMultipartReqDTO;
import issueissyu.backend.domain.pin.dto.req.StorePinImportReqDTO;
import issueissyu.backend.domain.pin.dto.res.StorePinImportResDTO;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface PinStoreCommandService {

    StorePinImportResDTO importStore(String uid, StorePinImportReqDTO request);

    StorePinImportResDTO importStoreV1(
            String uid,
            StorePinImportMultipartReqDTO request,
            List<MultipartFile> photos,
            MultipartFile storeProfileImage);
}
