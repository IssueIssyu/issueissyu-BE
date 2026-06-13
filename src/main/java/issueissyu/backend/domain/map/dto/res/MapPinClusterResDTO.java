package issueissyu.backend.domain.map.dto.res;

import java.util.List;

public record MapPinClusterResDTO(List<ClusterItemDTO> clusters) {

    public record ClusterItemDTO(
            long clusterId,
            double clusterLatitude,
            double clusterLongitude,
            int pinCount,
            List<MapPinResDTO.PinItemDTO> pins
    ) {
    }
}
