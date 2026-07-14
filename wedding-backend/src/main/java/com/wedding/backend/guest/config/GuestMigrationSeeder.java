package com.wedding.backend.guest.config;

import com.wedding.backend.guest.model.Guest;
import com.wedding.backend.guest.repository.GuestRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class GuestMigrationSeeder {

    private final GuestRepository guestRepository;

    public GuestMigrationSeeder(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(1)
    public void migrateExistingGuests() {
        List<Guest> guests = guestRepository.findAll();
        boolean changed = false;

        for (Guest guest : guests) {
            // Convidados legados (pré-cadastro inexistente) vieram da confirmação pública antiga.
            if (!guest.isPreRegistered() && !guest.isResponded()) {
                guest.setResponded(true);
                changed = true;
            }
        }

        if (changed) {
            guestRepository.saveAll(guests);
        }
    }
}
