package com.wedding.backend.guest.api;

import com.wedding.backend.guest.model.Guest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;

public final class GuestDtos {

    private GuestDtos() {
    }

    public record ConfirmGuestEntry(
            @NotNull Long id,
            @NotNull Boolean willAttend
    ) {
    }

    public record ConfirmGuestsRequest(
            List<@Valid ConfirmGuestEntry> guests
    ) {
    }

    public record GuestLookupResponse(
            Long id,
            String name,
            boolean godparent
    ) {
        public static GuestLookupResponse fromEntity(Guest guest) {
            return new GuestLookupResponse(guest.getId(), guest.getName(), guest.isGodparent());
        }
    }

    public record CreateGuestRequest(
            @NotBlank @Size(max = 150) String name,
            Boolean godparent
    ) {
    }

    public record UpdateGuestRequest(
            @NotBlank @Size(max = 150) String name,
            Boolean godparent,
            Boolean resetResponse
    ) {
    }

    public record GuestResponse(
            Long id,
            String name,
            boolean confirmed,
            boolean godparent,
            boolean responded,
            OffsetDateTime confirmationDate,
            OffsetDateTime createdAt
    ) {
        public static GuestResponse fromEntity(Guest guest) {
            return new GuestResponse(
                    guest.getId(),
                    guest.getName(),
                    guest.isConfirmed(),
                    guest.isGodparent(),
                    guest.isResponded(),
                    guest.getConfirmationDate(),
                    guest.getCreatedAt()
            );
        }
    }

    public record GuestPageResponse(
            List<GuestResponse> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
    }

    public record ImportGuestEntry(
            int row,
            @NotBlank @Size(max = 150) String name,
            @NotNull Boolean godparent
    ) {
    }

    public record ImportGuestsRequest(
            @NotNull List<@Valid ImportGuestEntry> guests
    ) {
    }

    public record ImportGuestRowError(
            int row,
            String name,
            String message
    ) {
    }

    public record ImportGuestsResponse(
            int created,
            List<ImportGuestRowError> errors
    ) {
    }
}
