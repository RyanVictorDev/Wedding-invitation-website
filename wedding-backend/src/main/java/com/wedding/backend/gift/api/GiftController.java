package com.wedding.backend.gift.api;

import com.wedding.backend.gift.api.GiftDtos.*;
import com.wedding.backend.gift.service.GiftService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gifts")
public class GiftController {

    private final GiftService giftService;

    public GiftController(GiftService giftService) {
        this.giftService = giftService;
    }

    @GetMapping
    public List<GiftResponse> listPublicGifts() {
        return giftService.listPublicGifts();
    }

    @GetMapping("/admin")
    public List<GiftResponse> listAllGifts() {
        return giftService.listAllGifts();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GiftResponse createGift(@Valid @RequestBody CreateGiftRequest request) {
        return giftService.createGift(request);
    }

    @PutMapping("/{id}")
    public GiftResponse updateGift(@PathVariable Long id, @Valid @RequestBody UpdateGiftRequest request) {
        return giftService.updateGift(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGift(@PathVariable Long id) {
        giftService.deleteGift(id);
    }

    @PostMapping("/{id}/reserve")
    public GiftResponse reserveGift(@PathVariable Long id, @RequestBody(required = false) ReserveGiftRequest request) {
        ReserveGiftRequest body = request != null ? request : new ReserveGiftRequest(null);
        return giftService.reserveGift(id, body);
    }

    @PostMapping("/preview-url")
    public PreviewUrlResponse previewUrl(@Valid @RequestBody PreviewUrlRequest request) {
        return giftService.previewUrl(request);
    }
}
