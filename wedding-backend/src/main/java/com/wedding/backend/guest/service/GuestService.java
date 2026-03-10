package com.wedding.backend.guest.service;

import com.wedding.backend.guest.api.GuestDtos.ConfirmGuestEntry;
import com.wedding.backend.guest.api.GuestDtos.ConfirmGuestsRequest;
import com.wedding.backend.guest.api.GuestDtos.GuestResponse;
import com.wedding.backend.guest.model.Guest;
import com.wedding.backend.guest.repository.GuestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class GuestService {

    private final GuestRepository guestRepository;

    public GuestService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    @Transactional
    public List<GuestResponse> confirmGuests(ConfirmGuestsRequest request) {
        OffsetDateTime now = OffsetDateTime.now();

        List<Guest> guests = request.guests().stream()
                .map(entry -> toGuest(entry, now))
                .toList();

        return guestRepository.saveAll(guests).stream()
                .map(GuestResponse::fromEntity)
                .toList();
    }

    private Guest toGuest(ConfirmGuestEntry entry, OffsetDateTime confirmationDate) {
        Guest guest = new Guest();
        guest.setName(entry.name());
        guest.setGodparent(entry.godparent() != null && entry.godparent());
        guest.setConfirmed(true);
        guest.setConfirmationDate(confirmationDate);
        guest.setCreatedAt(confirmationDate);
        return guest;
    }
}

