package com.wedding.backend.gift.service;

import com.wedding.backend.gift.api.GiftDtos.*;
import com.wedding.backend.gift.model.GiftItem;
import com.wedding.backend.gift.repository.GiftItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GiftService {

    private static final Pattern OG_TITLE = Pattern.compile(
            "<meta[^>]+property=[\"']og:title[\"'][^>]+content=[\"']([^\"']+)[\"']",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern OG_TITLE_ALT = Pattern.compile(
            "<meta[^>]+content=[\"']([^\"']+)[\"'][^>]+property=[\"']og:title[\"']",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern OG_DESC = Pattern.compile(
            "<meta[^>]+property=[\"']og:description[\"'][^>]+content=[\"']([^\"']+)[\"']",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern OG_DESC_ALT = Pattern.compile(
            "<meta[^>]+content=[\"']([^\"']+)[\"'][^>]+property=[\"']og:description[\"']",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern OG_IMAGE = Pattern.compile(
            "<meta[^>]+property=[\"']og:image[\"'][^>]+content=[\"']([^\"']+)[\"']",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern OG_IMAGE_ALT = Pattern.compile(
            "<meta[^>]+content=[\"']([^\"']+)[\"'][^>]+property=[\"']og:image[\"']",
            Pattern.CASE_INSENSITIVE
    );

    private final GiftItemRepository giftItemRepository;
    private final UrlPreviewService urlPreviewService;

    public GiftService(GiftItemRepository giftItemRepository, UrlPreviewService urlPreviewService) {
        this.giftItemRepository = giftItemRepository;
        this.urlPreviewService = urlPreviewService;
    }

    @Transactional(readOnly = true)
    public List<GiftResponse> listPublicGifts() {
        return giftItemRepository.findByActiveTrueOrderBySortOrderAscCreatedAtAsc().stream()
                .map(GiftResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GiftResponse> listAllGifts() {
        return giftItemRepository.findAllByOrderBySortOrderAscCreatedAtAsc().stream()
                .map(GiftResponse::fromEntity)
                .toList();
    }

    @Transactional
    public GiftResponse createGift(CreateGiftRequest request) {
        GiftItem item = new GiftItem();
        applyCreate(item, request);
        return GiftResponse.fromEntity(giftItemRepository.save(item));
    }

    @Transactional
    public GiftResponse updateGift(Long id, UpdateGiftRequest request) {
        GiftItem item = giftItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Presente não encontrado"));

        item.setTitle(request.title().trim());
        item.setDescription(blankToNull(request.description()));
        item.setImageUrl(blankToNull(request.imageUrl()));
        item.setProductUrl(blankToNull(request.productUrl()));
        item.setPrice(request.price());
        item.setActive(request.active());
        item.setSortOrder(request.sortOrder());

        if (request.clearReservation()) {
            item.setReserved(false);
            item.setReservedBy(null);
            item.setReservedAt(null);
        }

        return GiftResponse.fromEntity(giftItemRepository.save(item));
    }

    @Transactional
    public void deleteGift(Long id) {
        if (!giftItemRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Presente não encontrado");
        }
        giftItemRepository.deleteById(id);
    }

    @Transactional
    public GiftResponse reserveGift(Long id, ReserveGiftRequest request) {
        GiftItem item = giftItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Presente não encontrado"));

        if (!item.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Presente indisponível");
        }

        if (item.isReserved()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Presente já reservado");
        }

        item.setReserved(true);
        String reservedBy = request.reservedBy() != null ? request.reservedBy().trim() : null;
        item.setReservedBy(reservedBy != null && !reservedBy.isBlank() ? reservedBy : null);
        item.setReservedAt(OffsetDateTime.now());

        return GiftResponse.fromEntity(giftItemRepository.save(item));
    }

    public PreviewUrlResponse previewUrl(PreviewUrlRequest request) {
        String html = urlPreviewService.fetchHtml(request.url());
        return new PreviewUrlResponse(
                extractMeta(html, OG_TITLE, OG_TITLE_ALT),
                extractMeta(html, OG_DESC, OG_DESC_ALT),
                extractMeta(html, OG_IMAGE, OG_IMAGE_ALT)
        );
    }

    private void applyCreate(GiftItem item, CreateGiftRequest request) {
        item.setTitle(request.title().trim());
        item.setDescription(blankToNull(request.description()));
        item.setImageUrl(blankToNull(request.imageUrl()));
        item.setProductUrl(blankToNull(request.productUrl()));
        item.setPrice(request.price());
        item.setActive(request.active());
        item.setSortOrder(request.sortOrder());
        item.setReserved(false);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String extractMeta(String html, Pattern primary, Pattern alternate) {
        if (html == null || html.isBlank()) {
            return null;
        }
        Matcher matcher = primary.matcher(html);
        if (matcher.find()) {
            return decodeHtml(matcher.group(1));
        }
        matcher = alternate.matcher(html);
        if (matcher.find()) {
            return decodeHtml(matcher.group(1));
        }
        return null;
    }

    private String decodeHtml(String value) {
        return value
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">");
    }
}
