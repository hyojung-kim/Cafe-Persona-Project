package com.team.cafe.external.kakao;

import com.team.cafe.external.kakao.dto.KakaoAddressResponse;
import com.team.cafe.external.kakao.dto.KakaoKeywordResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class KakaoLocalClient {

    private final WebClient kakaoWebClient;

    public KakaoKeywordResponse searchKeyword(String query, Double x, Double y,
                                              Integer radius, Integer page, Integer size, String sort) {
        // /v2/local/search/keyword.json : query, x,y,radius,page(1..45),size(1..15),sort=accuracy|distance  [oai_citation:2‡Kakao Developers](https://developers.kakao.com/docs/latest/en/local/dev-guide)
        return kakaoWebClient.get()
                .uri(uri -> uri.path("/v2/local/search/keyword.json")
                        .queryParam("query", query)
                        .queryParamIfPresent("x", x == null ? null : java.util.Optional.of(x))
                        .queryParamIfPresent("y", y == null ? null : java.util.Optional.of(y))
                        .queryParamIfPresent("radius", radius == null ? null : java.util.Optional.of(radius))
                        .queryParam("page", page == null ? 1 : page)
                        .queryParam("size", size == null ? 15 : Math.min(size, 15))
                        .queryParam("sort", (sort == null || sort.isBlank()) ? "accuracy" : sort)
                        .build())
                .retrieve()
                .onStatus(HttpStatus::isError, r -> r.createException().flatMap(e -> {
                    return reactor.core.publisher.Mono.error(new RuntimeException("Kakao keyword search failed: " + e.getMessage()));
                }))
                .bodyToMono(KakaoKeywordResponse.class)
                .block();
    }

    public KakaoAddressResponse geocodeAddress(String query, Integer page, Integer size) {
        // /v2/local/search/address.json : query, page(1..45), size(1..30)  [oai_citation:3‡Kakao Developers](https://developers.kakao.com/docs/latest/en/local/dev-guide)
        return kakaoWebClient.get()
                .uri(uri -> uri.path("/v2/local/search/address.json")
                        .queryParam("query", query)
                        .queryParam("page", page == null ? 1 : page)
                        .queryParam("size", size == null ? 10 : Math.min(size, 30))
                        .build())
                .retrieve()
                .onStatus(HttpStatus::isError, r -> r.createException().flatMap(e -> {
                    return reactor.core.publisher.Mono.error(new RuntimeException("Kakao address search failed: " + e.getMessage()));
                }))
                .bodyToMono(KakaoAddressResponse.class)
                .block();
    }
}