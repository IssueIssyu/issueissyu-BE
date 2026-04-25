package issueissyu.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient naverRestClient(RestClient.Builder restClientBuilder) {
        // 네이버 API 전용 RestClient 빈.
        // 추후 timeout, interceptor, 로깅, 관측 설정을 이 빌더 체인에서 공통 관리할 수 있다.
        return restClientBuilder
                .baseUrl("https://openapi.naver.com")
                .build();
    }
}
