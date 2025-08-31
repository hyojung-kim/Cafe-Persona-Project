package com.team.cafe.external.kakao;

import com.team.cafe.external.kakao.dto.KakaoAddressResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;     // ✅ 중요
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class KakaoLocalClient {

    private final WebClient webClient; // WebClientConfig에서 주입

    @Value("${app.external.kakao.rest-api-key:a2ed26f1ff8c196702de6b2f52470d05}")
    private String kakaoApiKey;

    public Mono<KakaoAddressResponse> searchAddress(String query) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("dapi.kakao.com")
                        .path("/v2/local/search/address.json")
                        .queryParam("query", query)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoApiKey)
                .retrieve()
                // ✅ Spring 6 / Boot 3.3.3에 맞는 onStatus 시그니처
                .onStatus(HttpStatusCode::isError,
                        resp -> resp.createException().flatMap(Mono::error))
                .bodyToMono(KakaoAddressResponse.class);
    }
}