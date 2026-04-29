package issueissyu.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    @Primary
    public RestClient naverRestClient() {
        return RestClient.builder()
                .baseUrl("https://openapi.naver.com")
                .build();
    }

    @Bean
    public RestClient naverMapRestClient() {
        return RestClient.builder()
                .baseUrl("https://maps.apigw.ntruss.com")
                .build();
    }
}
