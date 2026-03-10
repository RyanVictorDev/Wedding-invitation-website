package com.wedding.backend.guest.api;

import com.wedding.backend.guest.model.Guest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;

public final class GuestDtos {

    private GuestDtos() {
    }

    public record ConfirmGuestEntry(
            @NotBlank
            @Size(max = 150)
            String name,
            Boolean godparent
    ) {
    }

    public record ConfirmGuestsRequest(
            List<@Valid ConfirmGuestEntry> guests
    ) {
    }

    public record GuestResponse(
            Long id,
            String name,
            boolean confirmed,
            boolean godparent,
            OffsetDateTime confirmationDate,
            OffsetDateTime createdAt
    ) {
        public static GuestResponse fromEntity(Guest guest) {
            return new GuestResponse(
                    guest.getId(),
                    guest.getName(),
                    guest.isConfirmed(),
                    guest.isGodparent(),
                    guest.getConfirmationDate(),
                    guest.getCreatedAt()
            );
        }
    }
}

