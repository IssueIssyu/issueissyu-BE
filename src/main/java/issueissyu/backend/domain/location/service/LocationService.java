package issueissyu.backend.domain.location.service;

import issueissyu.backend.domain.location.dto.res.NaverReverseGeocodeCodeAddressResDTO;
import issueissyu.backend.domain.location.dto.res.UserLocationCertResDto;
import issueissyu.backend.domain.location.dto.res.UserLocationResDTO;
import issueissyu.backend.domain.location.entity.Location;
import issueissyu.backend.domain.location.exception.LocationException;
import issueissyu.backend.domain.location.exception.code.LocationErrorCode;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import org.postgresql.geometric.PGpoint;
import org.springframework.stereotype.Service;
import issueissyu.backend.domain.location.repository.LocationRepository;
import issueissyu.backend.domain.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
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

    @Transactional(readOnly = true)
    public UserLocationResDTO isUserCanPostPin(String userId, PGpoint userPoint, PGpoint pinPoint){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        // 내부 좌표계: x=경도(lnt), y=위도(lat)
        double distanceInMeters = calDist(userPoint.y, userPoint.x, pinPoint.y, pinPoint.x);

        if(distanceInMeters <= 100){
            return new UserLocationResDTO(naverMapService.resolveLegalDistrictCodeAndAddress(pinPoint).address());
        }
        NaverReverseGeocodeCodeAddressResDTO resolved = naverMapService.resolveLegalDistrictCodeAndAddress(pinPoint);
        if(user.getUserLocation().getLocation().getLocationId().equals(
                locationRepository.findAllByLocationSigunguPrefix(resolved.legalDistrictCode().substring(0, 5)).getFirst().getLocationId())
        ){
            return new UserLocationResDTO(naverMapService.resolveLegalDistrictCodeAndAddress(pinPoint).address());
        }

        throw LocationException.of(LocationErrorCode.LOCATION_PIN_CREATION_FORBIDDEN);

    }



    private Double calDist(Double userLat,Double userLng, Double pinLat, Double pinLng){
        GeodesicData geodesicData = Geodesic.WGS84.Inverse(userLat, userLng, pinLat, pinLng);
        return geodesicData.s12;
    }
}
