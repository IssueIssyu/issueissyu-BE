package issueissyu.backend.domain.location.service.query;

import issueissyu.backend.domain.location.converter.LocationConverter;
import issueissyu.backend.domain.location.dto.res.LocationRegionResDTO;
import issueissyu.backend.domain.location.entity.Location;
import issueissyu.backend.domain.location.exception.LocationException;
import issueissyu.backend.domain.location.exception.code.LocationErrorCode;
import issueissyu.backend.domain.location.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationQueryServiceImpl implements LocationQueryService {

    private final LocationRepository locationRepository;

    @Override
    public LocationRegionResDTO getRegionByLocationId(Long locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> LocationException.of(LocationErrorCode.LOCATION_REGION_404));
        return LocationConverter.toRegionResDTO(location);
    }
}
