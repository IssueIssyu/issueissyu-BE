package issueissyu.backend.domain.location.dto.res;

import java.util.List;

public record LocationRegionGroupResDTO(String superLocation, List<LocationRegionItemResDTO> subLocation) {}
