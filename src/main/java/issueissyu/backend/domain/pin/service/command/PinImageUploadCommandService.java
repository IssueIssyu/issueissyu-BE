package issueissyu.backend.domain.pin.service.command;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface PinImageUploadCommandService {

    List<String> uploadPinImages(List<MultipartFile> photos);
}
