package issueissyu.backend.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "naver.maps")
public class NaverMapProperties {

    private String clientId;
    private String clientSecret;
}
