package com.wedding.backend.payment.abacatepay;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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

        try {
            return restClient.post()
                    .uri("/pixQrCode/create")
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw mapProviderError(response.getStatusCode());
                    })
                    .body(PixQrCodeCreateResponse.class);
        } catch (AbacatePayException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AbacatePayException(
                    HttpStatus.BAD_GATEWAY,
                    "Falha ao gerar PIX na AbacatePay"
            );
        }
    }

    public PixQrCodeStatusResponse checkPixStatus(String pixId) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/pixQrCode/check")
                            .queryParam("id", pixId)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw mapProviderError(response.getStatusCode());
                    })
                    .body(PixQrCodeStatusResponse.class);
        } catch (AbacatePayException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AbacatePayException(
                    HttpStatus.BAD_GATEWAY,
                    "Falha ao consultar status do PIX na AbacatePay"
            );
        }
    }

    private static AbacatePayException mapProviderError(HttpStatusCode statusCode) {
        if (statusCode.value() == 401 || statusCode.value() == 403) {
            return new AbacatePayException(
                    HttpStatus.BAD_GATEWAY,
                    "Chave da AbacatePay inválida ou inativa. Verifique ABACATEPAY_API_KEY no .env"
            );
        }
        return new AbacatePayException(
                HttpStatus.BAD_GATEWAY,
                "AbacatePay recusou a criação do PIX (HTTP " + statusCode.value() + ")"
        );
    }
}


