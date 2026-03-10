package com.wedding.backend.payment.abacatepay;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Component
public class AbacatePayClient {

    private final RestClient restClient;

    public AbacatePayClient(
            @Value("${abacatepay.base-url}") String baseUrl,
            @Value("${abacatepay.api-key}") String apiKey
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl + "/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public PixQrCodeCreateResponse createPixQrCode(long amountInCents, String description) {
        Map<String, Object> body = new HashMap<>();
        body.put("amount", amountInCents);
        body.put("expiresIn", 900); // 15 minutos
        if (description != null && !description.isBlank()) {
            body.put("description", description);
        }

        return restClient.post()
                .uri("/pixQrCode/create")
                .body(body)
                .retrieve()
                .body(PixQrCodeCreateResponse.class);
    }

    public PixQrCodeStatusResponse checkPixStatus(String pixId) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/pixQrCode/check")
                        .queryParam("id", pixId)
                        .build())
                .retrieve()
                .body(PixQrCodeStatusResponse.class);
    }
}


