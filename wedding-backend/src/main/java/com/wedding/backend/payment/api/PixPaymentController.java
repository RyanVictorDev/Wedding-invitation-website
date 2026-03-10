package com.wedding.backend.payment.api;

import com.wedding.backend.payment.api.PaymentDtos.CreatePaymentRequest;
import com.wedding.backend.payment.api.PaymentDtos.PixPaymentResponse;
import com.wedding.backend.payment.api.PaymentDtos.PixPaymentStatusResponse;
import com.wedding.backend.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/pix")
public class PixPaymentController {

    private final PaymentService paymentService;

    public PixPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PixPaymentResponse createPixPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.createPixPayment(request);
    }

    @GetMapping("/{pixId}/status")
    public PixPaymentStatusResponse getPixStatus(@PathVariable String pixId) {
        return paymentService.checkPixStatus(pixId);
    }
}

