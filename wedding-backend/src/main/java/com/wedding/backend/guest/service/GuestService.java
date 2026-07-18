package com.wedding.backend.guest.service;

import com.wedding.backend.guest.api.GuestDtos.*;
import com.wedding.backend.guest.model.Guest;
import com.wedding.backend.guest.repository.GuestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class GuestService {

    private final GuestRepository guestRepository;

    public GuestService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    @Transactional(readOnly = true)
    public List<GuestLookupResponse> lookupGuests(String search) {
        if (search == null || search.trim().length() < 3) {
            return List.of();
        }

        return guestRepository.findByRespondedFalseAndNameContainingIgnoreCaseOrderByNameAsc(search.trim())
                .stream()
                .map(GuestLookupResponse::fromEntity)
                .toList();
    }

    @Transactional
    public List<GuestResponse> confirmGuests(ConfirmGuestsRequest request) {
        if (request.guests() == null || request.guests().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe ao menos um convidado");
        }

        OffsetDateTime now = OffsetDateTime.now();
        Set<Long> seenIds = new HashSet<>();

        return request.guests().stream()
                .map(entry -> confirmSingleGuest(entry, now, seenIds))
                .map(GuestResponse::fromEntity)
                .toList();
    }

    private Guest confirmSingleGuest(ConfirmGuestEntry entry, OffsetDateTime now, Set<Long> seenIds) {
        if (!seenIds.add(entry.id())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Convidado duplicado na confirmação");
        }

        Guest guest = guestRepository.findById(entry.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Convidado não encontrado"));

        if (guest.isResponded()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este convidado já confirmou presença");
        }

        boolean willAttend = entry.willAttend() != null && entry.willAttend();
        guest.setResponded(true);
        guest.setConfirmed(willAttend);
        guest.setConfirmationDate(willAttend ? now : null);

        return guestRepository.save(guest);
    }

    @Transactional
    public GuestResponse createGuest(CreateGuestRequest request) {
        if (guestRepository.existsByNameIgnoreCase(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um convidado com este nome");
        }

        Guest guest = new Guest();
        guest.setName(request.name().trim());
        guest.setGodparent(request.godparent() != null && request.godparent());
        guest.setConfirmed(false);
        guest.setResponded(false);
        guest.setPreRegistered(true);
        guest.setConfirmationDate(null);
        guest.setCreatedAt(OffsetDateTime.now());

        return GuestResponse.fromEntity(guestRepository.save(guest));
    }

    @Transactional
    public GuestResponse updateGuest(Long id, UpdateGuestRequest request) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Convidado não encontrado"));

        if (guestRepository.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um convidado com este nome");
        }

        guest.setName(request.name().trim());
        guest.setGodparent(request.godparent() != null && request.godparent());

        if (Boolean.TRUE.equals(request.resetResponse())) {
            guest.setResponded(false);
            guest.setConfirmed(false);
            guest.setConfirmationDate(null);
        }

        return GuestResponse.fromEntity(guestRepository.save(guest));
    }

    @Transactional
    public void deleteGuest(Long id) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Convidado não encontrado"));

        if (guest.isResponded()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível excluir convidado que já respondeu");
        }

        guestRepository.delete(guest);
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

        if ("pending".equalsIgnoreCase(status)) {
            result = hasSearch
                    ? guestRepository.findByRespondedAndNameContainingIgnoreCase(false, term, pageable)
                    : guestRepository.findByResponded(false, pageable);
        } else if ("yes".equalsIgnoreCase(status)) {
            result = hasSearch
                    ? guestRepository.findByRespondedAndConfirmedAndNameContainingIgnoreCase(true, true, term, pageable)
                    : guestRepository.findByRespondedAndConfirmed(true, true, pageable);
        } else if ("no".equalsIgnoreCase(status)) {
            result = hasSearch
                    ? guestRepository.findByRespondedAndConfirmedAndNameContainingIgnoreCase(true, false, term, pageable)
                    : guestRepository.findByRespondedAndConfirmed(true, false, pageable);
        } else {
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

    @Transactional
    public ImportGuestsResponse importGuests(ImportGuestsRequest request) {
        if (request.guests() == null || request.guests().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe ao menos um convidado");
        }

        List<ImportGuestRowError> errors = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();

        for (ImportGuestEntry entry : request.guests()) {
            List<String> rowErrors = validateImportEntry(entry, seenNames);
            if (!rowErrors.isEmpty()) {
                errors.add(new ImportGuestRowError(
                        entry.row(),
                        entry.name(),
                        String.join("; ", rowErrors)
                ));
            }
        }

        if (!errors.isEmpty()) {
            return new ImportGuestsResponse(0, errors);
        }

        OffsetDateTime now = OffsetDateTime.now();

        for (ImportGuestEntry entry : request.guests()) {
            Guest guest = new Guest();
            guest.setName(entry.name().trim());
            guest.setGodparent(entry.godparent());
            guest.setConfirmed(false);
            guest.setResponded(false);
            guest.setPreRegistered(true);
            guest.setConfirmationDate(null);
            guest.setCreatedAt(now);
            guestRepository.save(guest);
        }

        return new ImportGuestsResponse(request.guests().size(), List.of());
    }

    private List<String> validateImportEntry(ImportGuestEntry entry, Set<String> seenNames) {
        List<String> rowErrors = new ArrayList<>();
        String name = entry.name() != null ? entry.name().trim() : "";

        if (name.isBlank()) {
            rowErrors.add("Nome é obrigatório");
        } else if (name.length() > 150) {
            rowErrors.add("Nome deve ter no máximo 150 caracteres");
        }

        if (entry.godparent() == null) {
            rowErrors.add("Padrinho/Madrinha é obrigatório");
        }

        if (!name.isBlank()) {
            String key = name.toLowerCase(Locale.ROOT);
            if (!seenNames.add(key)) {
                rowErrors.add("Nome duplicado na importação");
            }
            if (guestRepository.existsByNameIgnoreCase(name)) {
                rowErrors.add("Já existe um convidado com este nome");
            }
        }

        return rowErrors;
    }
}
