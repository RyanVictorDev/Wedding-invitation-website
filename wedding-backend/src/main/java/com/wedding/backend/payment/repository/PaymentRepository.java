package com.wedding.backend.payment.repository;

import com.wedding.backend.payment.model.Payment;
import com.wedding.backend.payment.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByProviderPaymentId(String providerPaymentId);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
}

