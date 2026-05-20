package issueissyu.backend.domain.pin.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CommunicationPinEditMultipartReqDTO(
        @NotNull @Valid @Size(max = 5) List<CommunicationPinImportMultipartImageReqDTO> pinImages,
        @NotBlank String pinTitle,
        @NotBlank String pinContent) {}
