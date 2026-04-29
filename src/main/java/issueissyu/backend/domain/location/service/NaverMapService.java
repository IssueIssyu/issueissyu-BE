package issueissyu.backend.domain.location.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import issueissyu.backend.domain.location.service.command.NaverMapReverseGeocodeService;
import issueissyu.backend.domain.location.service.command.NaverMapGeocodeService;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.


@Service
@RequiredArgsConstructor
public class NaverMapService {
    private final NaverMapReverseGeocodeService naverMapReverseGeocodeService;
    private final NaverMapGeocodeService naverMapGeocodeService;


}
