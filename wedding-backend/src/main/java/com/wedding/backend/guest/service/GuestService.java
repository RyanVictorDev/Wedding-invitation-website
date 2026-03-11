package com.wedding.backend.guest.service;

import com.wedding.backend.guest.api.GuestDtos.ConfirmGuestEntry;
import com.wedding.backend.guest.api.GuestDtos.ConfirmGuestsRequest;
import com.wedding.backend.guest.api.GuestDtos.GuestPageResponse;
import com.wedding.backend.guest.api.GuestDtos.GuestResponse;
import com.wedding.backend.guest.model.Guest;
import com.wedding.backend.guest.repository.GuestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        boolean willAttend = entry.willAttend() == null || entry.willAttend();
        guest.setConfirmed(willAttend);
        guest.setConfirmationDate(willAttend ? confirmationDate : null);
        guest.setCreatedAt(confirmationDate);
        return guest;
    }

    @Transactional(readOnly = true)
    public GuestPageResponse listGuests(int page, int size, String search, String status) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.ASC, "name"))
        );

        Page<Guest> result;
        boolean hasSearch = search != null && !search.isBlank();
        String term = hasSearch ? search.trim() : null;

        boolean confirmedFilter;
        if ("yes".equalsIgnoreCase(status)) {
            confirmedFilter = true;
            result = hasSearch
                    ? guestRepository.findByConfirmedAndNameContainingIgnoreCase(true, term, pageable)
                    : guestRepository.findByConfirmed(true, pageable);
        } else if ("no".equalsIgnoreCase(status)) {
            confirmedFilter = false;
            result = hasSearch
                    ? guestRepository.findByConfirmedAndNameContainingIgnoreCase(false, term, pageable)
                    : guestRepository.findByConfirmed(false, pageable);
        } else {
            // all
            result = hasSearch
                    ? guestRepository.findByNameContainingIgnoreCase(term, pageable)
                    : guestRepository.findAll(pageable);
        }

        List<GuestResponse> content = result.map(GuestResponse::fromEntity).getContent();

        return new GuestPageResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}

