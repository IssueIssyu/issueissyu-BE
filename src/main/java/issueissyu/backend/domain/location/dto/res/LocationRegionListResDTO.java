package issueissyu.backend.domain.location.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record LocationRegionListResDTO(
        @JsonProperty("user") UserRegionSnippetResDTO user,
        List<LocationRegionItemResDTO> locations) {}
