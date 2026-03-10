package com.wedding.backend.dashboard.api;

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
            String confirmationDate
    ) {
    }

    public record DashboardSummaryResponse(
            long totalGuests,
            long confirmedGuests,
            long unconfirmedGuests,
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

