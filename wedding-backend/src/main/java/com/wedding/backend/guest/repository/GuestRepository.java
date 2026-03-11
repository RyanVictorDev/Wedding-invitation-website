package com.wedding.backend.guest.repository;

import com.wedding.backend.guest.model.Guest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {

    Page<Guest> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Guest> findByConfirmed(boolean confirmed, Pageable pageable);

    Page<Guest> findByConfirmedAndNameContainingIgnoreCase(boolean confirmed, String name, Pageable pageable);
}

