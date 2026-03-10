package com.wedding.backend.dashboard.service;

import com.wedding.backend.dashboard.api.DashboardDtos.DashboardSummaryResponse;
import com.wedding.backend.dashboard.api.DashboardDtos.GuestSummary;
import com.wedding.backend.guest.model.Guest;
import com.wedding.backend.guest.repository.GuestRepository;
import com.wedding.backend.payment.model.Payment;
import com.wedding.backend.payment.model.PaymentStatus;
import com.wedding.backend.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardService {

    private final GuestRepository guestRepository;
    private final PaymentRepository paymentRepository;

    public DashboardService(GuestRepository guestRepository, PaymentRepository paymentRepository) {
        this.guestRepository = guestRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        List<Guest> guests = guestRepository.findAll();
        List<Payment> payments = paymentRepository.findAll();

        long totalGuests = guests.size();
        long confirmedGuests = guests.stream().filter(Guest::isConfirmed).count();
        long unconfirmedGuests = totalGuests - confirmedGuests;
        long godparents = guests.stream().filter(Guest::isGodparent).count();

        BigDecimal totalPayments = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalPaymentsCount = payments.size();

        BigDecimal paidTotal = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long paidCount = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .count();

        BigDecimal pendingTotal = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingCount = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .count();

        List<GuestSummary> guestSummaries = guests.stream()
                .map(g -> new GuestSummary(
                        g.getId(),
                        g.getName(),
                        g.isConfirmed(),
                        g.isGodparent(),
                        g.getConfirmationDate() != null ? g.getConfirmationDate().toString() : null
                ))
                .toList();

        return new DashboardSummaryResponse(
                totalGuests,
                confirmedGuests,
                unconfirmedGuests,
                godparents,
                totalPayments,
                totalPaymentsCount,
                paidTotal,
                paidCount,
                pendingTotal,
                pendingCount,
                guestSummaries
        );
    }
}

