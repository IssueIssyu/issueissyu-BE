package issueissyu.backend.domain.map.dto.res;

public interface MapPinClusterView {
    Long getPinId();

    String getPinType();

    Double getLat();

    Double getLng();

    String getDetailAddress();

    String getRegion();

    Double getClusterLat();

    Double getClusterLng();
}
