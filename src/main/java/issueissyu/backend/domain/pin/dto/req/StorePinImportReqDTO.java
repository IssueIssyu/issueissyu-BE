package issueissyu.backend.domain.pin.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public record StorePinImportReqDTO(
        @NotNull Double lat,
        @NotNull Double lng,
        @NotNull @Valid @Size(max = 5) List<PinImageItemReqDTO> pinImageUrls,
        @NotBlank String pinTitle,
        @NotBlank String pinContent,
        @NotNull LocalDateTime eventStartTime,
        @NotNull LocalDateTime eventEndTime,
        @NotBlank String discount,
        @NotBlank String storeProfileImageUrl) {}
