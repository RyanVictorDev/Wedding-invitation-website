package com.wedding.backend.gift.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UrlPreviewService {

    private final RestClient restClient;

    public UrlPreviewService() {
        this.restClient = RestClient.builder()
                .defaultHeader("User-Agent", "Mozilla/5.0 (compatible; WeddingBot/1.0)")
                .build();
    }

    public String fetchHtml(String url) {
        try {
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não foi possível carregar a URL informada");
        }
    }
}
