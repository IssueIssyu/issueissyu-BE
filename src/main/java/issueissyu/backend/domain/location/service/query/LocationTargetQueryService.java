package issueissyu.backend.domain.location.service.query;

import issueissyu.backend.domain.location.entity.PinLocation;
import issueissyu.backend.domain.location.entity.PopulationDensity;
import issueissyu.backend.domain.location.repository.PinLocationRepository;
import issueissyu.backend.domain.location.repository.PopulationDensityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationTargetQueryService {

    private static final int DEFAULT_TARGET_PETITION = 30;
    private static final int DEFAULT_TARGET_COMMUNITY = 10;

    private final PinLocationRepository pinLocationRepository;
    private final PopulationDensityRepository populationDensityRepository;

    public int getTargetPetitionByPinId(Long pinId) {
        return pinLocationRepository.findFirstByPin_PinId(pinId)
                .flatMap(pinLocation -> populationDensityRepository
                        .findByLocation_LocationId(pinLocation.getLocation().getLocationId()))
                .map(PopulationDensity::getTargetPetition)
                .orElse(DEFAULT_TARGET_PETITION);
    }

    public int getTargetCommunityByPinId(Long pinId) {
        return pinLocationRepository.findFirstByPin_PinId(pinId)
                .flatMap(pinLocation -> populationDensityRepository
                        .findByLocation_LocationId(pinLocation.getLocation().getLocationId()))
                .map(PopulationDensity::getTargetCommunity)
                .orElse(DEFAULT_TARGET_COMMUNITY);
    }
}