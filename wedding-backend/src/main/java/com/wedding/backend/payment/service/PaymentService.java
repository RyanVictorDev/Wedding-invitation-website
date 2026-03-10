package com.wedding.backend.payment.service;

import com.wedding.backend.payment.abacatepay.AbacatePayClient;
import com.wedding.backend.payment.abacatepay.PixQrCodeCreateResponse;
import com.wedding.backend.payment.abacatepay.PixQrCodeStatusResponse;
import com.wedding.backend.payment.api.PaymentDtos.CreatePaymentRequest;
import com.wedding.backend.payment.api.PaymentDtos.PixPaymentResponse;
import com.wedding.backend.payment.api.PaymentDtos.PixPaymentStatusResponse;
import com.wedding.backend.payment.model.Payment;
import com.wedding.backend.payment.model.PaymentStatus;
import com.wedding.backend.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AbacatePayClient abacatePayClient;

    public PaymentService(PaymentRepository paymentRepository,
                          AbacatePayClient abacatePayClient) {
        this.paymentRepository = paymentRepository;
        this.abacatePayClient = abacatePayClient;
    }

    @Transactional
    public PixPaymentResponse createPixPayment(CreatePaymentRequest request) {
        Payment payment = new Payment();
        payment.setAmount(request.amount());
        payment.setCurrency(request.currency() != null ? request.currency() : "BRL");
        payment.setPayerName(request.payerName());
        payment.setDescription(request.description());
        payment.setStatus(PaymentStatus.PENDING);

        long amountInCents = request.amount()
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        PixQrCodeCreateResponse providerResponse =
                abacatePayClient.createPixQrCode(amountInCents, request.description());

        if (providerResponse != null && providerResponse.getData() != null) {
            PixQrCodeCreateResponse.PixQrCode data = providerResponse.getData();
            payment.setProviderPaymentId(data.getId());
            payment.setStatus(mapStatus(data.getStatus()));
            Payment saved = paymentRepository.save(payment);

            return new PixPaymentResponse(
                    saved.getId(),
                    data.getId(),
                    data.getStatus(),
                    data.getBrCode(),
                    data.getBrCodeBase64()
            );
        }

        Payment saved = paymentRepository.save(payment);
        return new PixPaymentResponse(
                saved.getId(),
                null,
                saved.getStatus().name(),
                null,
                null
        );
    }

    @Transactional
    public PixPaymentStatusResponse checkPixStatus(String pixId) {
        PixQrCodeStatusResponse response = abacatePayClient.checkPixStatus(pixId);
        final String[] statusHolder = {null};
        String expiresAt = null;

        if (response != null && response.getData() != null) {
            statusHolder[0] = response.getData().getStatus();
            expiresAt = response.getData().getExpiresAt();

            final String finalStatus = statusHolder[0];
            paymentRepository.findByProviderPaymentId(pixId)
                    .ifPresent(payment -> {
                        payment.setStatus(mapStatus(finalStatus));
                        paymentRepository.save(payment);
                    });
        }

        return new PixPaymentStatusResponse(statusHolder[0], expiresAt);
    }

    private PaymentStatus mapStatus(String providerStatus) {
        if (providerStatus == null) {
            return PaymentStatus.PENDING;
        }
        return switch (providerStatus.toUpperCase()) {
            case "PAID" -> PaymentStatus.PAID;
            case "EXPIRED", "CANCELLED", "REFUNDED" -> PaymentStatus.FAILED;
            default -> PaymentStatus.PENDING;
        };
    }
}

