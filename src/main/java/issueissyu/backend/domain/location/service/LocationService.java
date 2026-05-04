package issueissyu.backend.domain.location.service;

import issueissyu.backend.domain.location.dto.res.NaverReverseGeocodeCodeAddressResDTO;
import issueissyu.backend.domain.location.dto.res.UserLocationCertResDto;
import issueissyu.backend.domain.location.entity.Location;
import issueissyu.backend.domain.location.exception.LocationException;
import issueissyu.backend.domain.location.exception.code.LocationErrorCode;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.exception.UserNotFoundException;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.postgresql.geometric.PGpoint;
import org.springframework.stereotype.Service;
import issueissyu.backend.domain.location.repository.LocationRepository;
import issueissyu.backend.domain.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final NaverMapService naverMapService;


    @Transactional
    public UserLocationCertResDto userLocationCert(String userId, PGpoint point) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        NaverReverseGeocodeCodeAddressResDTO resolved = naverMapService.resolveLegalDistrictCodeAndAddress(point);
        Location userLocation = locationRepository.findAllByLocationSigunguPrefix(resolved.legalDistrictCode().substring(0, 5)).getFirst();
        if(userLocation == null){
            throw LocationException.of(LocationErrorCode.LOCATION_LEGAL_DISTRICT_CODE_NOT_FOUND);
        }
        user.setUserLocation(userLocation,point);
        return UserLocationCertResDto.from(user.getUserLocation().getLocation());
    }

    @Transactional(readOnly = true)
    public UserLocationCertResDto getUserLocation(String userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));
        if(user.getUserLocation() == null){
            throw LocationException.of(LocationErrorCode.LOCATION_LEGAL_DISTRICT_CODE_NOT_FOUND);
        }
        return UserLocationCertResDto.from(user.getUserLocation().getLocation());
    }
}
