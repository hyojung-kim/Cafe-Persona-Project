package com.team.cafe.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${app.external.kakao-map.rest-api-key}")
    private String kakaoRestApiKey;

    @Bean(name = "kakaoWebClient")
    public WebClient kakaoWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(5))
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(5)));

        return WebClient.builder()
                .baseUrl("https://dapi.kakao.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoRestApiKey) // 필수 헤더  [oai_citation:1‡Kakao Developers](https://developers.kakao.com/docs/latest/en/local/dev-guide)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                        .build())
                .build();
    }
}
