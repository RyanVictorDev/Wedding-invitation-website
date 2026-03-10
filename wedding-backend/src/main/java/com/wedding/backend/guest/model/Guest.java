package com.wedding.backend.guest.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "guests")
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private boolean confirmed;

    @Column(nullable = false)
    private boolean godparent;

    @Column
    private OffsetDateTime confirmationDate;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        // Se o convidado é criado no fluxo de confirmação,
        // por padrão já consideramos confirmado.
        if (!confirmed) {
            confirmed = true;
        }
        if (confirmationDate == null && confirmed) {
            confirmationDate = OffsetDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public boolean isGodparent() {
        return godparent;
    }

    public void setGodparent(boolean godparent) {
        this.godparent = godparent;
    }

    public OffsetDateTime getConfirmationDate() {
        return confirmationDate;
    }

    public void setConfirmationDate(OffsetDateTime confirmationDate) {
        this.confirmationDate = confirmationDate;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

