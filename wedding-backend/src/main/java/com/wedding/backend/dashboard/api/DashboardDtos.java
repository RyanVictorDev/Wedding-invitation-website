package com.wedding.backend.dashboard.api;

import com.wedding.backend.guest.model.AttendanceType;
import com.wedding.backend.guest.model.GuestAgeCategory;

import java.math.BigDecimal;
import java.util.List;

public final class DashboardDtos {

    private DashboardDtos() {
    }

    public record GuestSummary(
            Long id,
            String name,
            boolean confirmed,
            boolean godparent,
            boolean responded,
            AttendanceType attendanceType,
            GuestAgeCategory ageCategory,
            String confirmationDate
    ) {
    }

    public record DashboardSummaryResponse(
            long totalGuests,
            long confirmedGuests,
            long unconfirmedGuests,
            long pendingGuests,
            long godparents,
            BigDecimal totalPayments,
            long totalPaymentsCount,
            BigDecimal paidPaymentsTotal,
            long paidPaymentsCount,
            BigDecimal pendingPaymentsTotal,
            long pendingPaymentsCount,
            List<GuestSummary> guests
    ) {
    }
}

