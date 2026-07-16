package com.wedding.backend.payment.abacatepay;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AbacatePayException extends ResponseStatusException {

    public AbacatePayException(HttpStatus status, String reason) {
        super(status, reason);
    }
}
