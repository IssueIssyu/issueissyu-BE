package issueissyu.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
    @Bean
    public RestClient naverRestClient() {
        return RestClient.builder()
                .baseUrl("https://openapi.naver.com")
                .build();
    }

    @Bean
    public RestClient naverMapRestClient(RestClient.Builder restClientBuilder) {
        return RestClient.builder()
                .baseUrl("https://maps.apigw.ntruss.com")
                .build();
    }

}
