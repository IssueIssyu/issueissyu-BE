package issueissyu.backend.domain.pin.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record CommunicationPinImportMultipartImageReqDTO(
        @NotNull @JsonProperty("isMain") Boolean isMain) {}
