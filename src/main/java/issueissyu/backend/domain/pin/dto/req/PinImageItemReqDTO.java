package issueissyu.backend.domain.pin.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PinImageItemReqDTO(
        @NotBlank String pinImageUrl,
        @NotNull @JsonProperty("isMain") boolean isMain) {}
