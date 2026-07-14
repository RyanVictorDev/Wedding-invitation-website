package com.wedding.backend.guest.repository;

import com.wedding.backend.guest.model.Guest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {

    Page<Guest> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Guest> findByConfirmed(boolean confirmed, Pageable pageable);

    Page<Guest> findByConfirmedAndNameContainingIgnoreCase(boolean confirmed, String name, Pageable pageable);

    Page<Guest> findByResponded(boolean responded, Pageable pageable);

    Page<Guest> findByRespondedAndNameContainingIgnoreCase(boolean responded, String name, Pageable pageable);

    Page<Guest> findByRespondedAndConfirmed(boolean responded, boolean confirmed, Pageable pageable);

    Page<Guest> findByRespondedAndConfirmedAndNameContainingIgnoreCase(
            boolean responded, boolean confirmed, String name, Pageable pageable
    );

    List<Guest> findByRespondedFalseAndNameContainingIgnoreCaseOrderByNameAsc(String name);

    Optional<Guest> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
