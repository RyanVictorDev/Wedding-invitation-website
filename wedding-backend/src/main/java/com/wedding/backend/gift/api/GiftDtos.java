package com.wedding.backend.gift.api;

import com.wedding.backend.gift.model.GiftItem;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public final class GiftDtos {

    private GiftDtos() {
    }

    public record GiftResponse(
            Long id,
            String title,
            String description,
            String imageUrl,
            String productUrl,
            BigDecimal price,
            boolean reserved,
            String reservedBy,
            OffsetDateTime reservedAt,
            boolean active,
            int sortOrder,
            OffsetDateTime createdAt
    ) {
        public static GiftResponse fromEntity(GiftItem item) {
            return new GiftResponse(
                    item.getId(),
                    item.getTitle(),
                    item.getDescription(),
                    item.getImageUrl(),
                    item.getProductUrl(),
                    item.getPrice(),
                    item.isReserved(),
                    item.getReservedBy(),
                    item.getReservedAt(),
                    item.isActive(),
                    item.getSortOrder(),
                    item.getCreatedAt()
            );
        }
    }

    public record CreateGiftRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 1000) String description,
            @Size(max = 2048) String imageUrl,
            @Size(max = 2048) String productUrl,
            @DecimalMin("0.0") BigDecimal price,
            boolean active,
            int sortOrder
    ) {
    }

    public record UpdateGiftRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 1000) String description,
            @Size(max = 2048) String imageUrl,
            @Size(max = 2048) String productUrl,
            @DecimalMin("0.0") BigDecimal price,
            boolean active,
            int sortOrder,
            boolean clearReservation
    ) {
    }

    public record ReserveGiftRequest(
            @Size(max = 150) String reservedBy
    ) {
    }

    public record PreviewUrlRequest(
            @NotBlank @Size(max = 2048) String url
    ) {
    }

    public record PreviewUrlResponse(
            String title,
            String description,
            String imageUrl
    ) {
    }
}
