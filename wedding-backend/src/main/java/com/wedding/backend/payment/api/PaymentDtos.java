package com.wedding.backend.payment.api;

import com.wedding.backend.payment.model.Payment;
import com.wedding.backend.payment.model.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public final class PaymentDtos {

    private PaymentDtos() {
    }

    public record CreatePaymentRequest(
            @NotNull
            @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
            BigDecimal amount,

            @Size(max = 3)
            String currency,

            @NotBlank
            @Size(max = 150)
            String payerName,

            @Size(max = 255)
            String description
    ) {
    }

    public record PaymentResponse(
            Long id,
            BigDecimal amount,
            String currency,
            String payerName,
            String description,
            PaymentStatus status,
            String providerPaymentId,
            OffsetDateTime createdAt
    ) {
        public static PaymentResponse fromEntity(Payment payment) {
            return new PaymentResponse(
                    payment.getId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getPayerName(),
                    payment.getDescription(),
                    payment.getStatus(),
                    payment.getProviderPaymentId(),
                    payment.getCreatedAt()
            );
        }
    }

    public record PixPaymentResponse(
            Long paymentId,
            String pixId,
            String status,
            String brCode,
            String brCodeBase64
    ) {
    }

    public record PixPaymentStatusResponse(
            String status,
            String expiresAt
    ) {
    }

}


