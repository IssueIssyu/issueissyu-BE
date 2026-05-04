package issueissyu.backend.domain.location.service;

import issueissyu.backend.domain.location.dto.req.NaverReverseGeocodeReqDTO;
import issueissyu.backend.domain.location.dto.res.NaverReverseGeocodeResDTO;
import lombok.RequiredArgsConstructor;
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

    @Transactional(readOnly = true)
    public NaverReverseGeocodeResDTO canPostPin(String uid)



}
