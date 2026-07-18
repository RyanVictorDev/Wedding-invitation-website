package com.wedding.backend.guest.api;

import com.wedding.backend.guest.api.GuestDtos.*;
import com.wedding.backend.guest.service.GuestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guests")
public class GuestController {

    private final GuestService guestService;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    @GetMapping("/lookup")
    public List<GuestLookupResponse> lookupGuests(@RequestParam String search) {
        return guestService.lookupGuests(search);
    }

    @PostMapping("/confirm")
    @ResponseStatus(HttpStatus.OK)
    public List<GuestResponse> confirmGuests(@Valid @RequestBody ConfirmGuestsRequest request) {
        return guestService.confirmGuests(request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GuestResponse createGuest(@Valid @RequestBody CreateGuestRequest request) {
        return guestService.createGuest(request);
    }

    @PutMapping("/{id}")
    public GuestResponse updateGuest(@PathVariable Long id, @Valid @RequestBody UpdateGuestRequest request) {
        return guestService.updateGuest(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGuest(@PathVariable Long id) {
        guestService.deleteGuest(id);
    }

    @GetMapping
    public GuestPageResponse listGuests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "all") String status
    ) {
        return guestService.listGuests(page, size, search, status);
    }

    @PostMapping("/import")
    @ResponseStatus(HttpStatus.CREATED)
    public ImportGuestsResponse importGuests(@Valid @RequestBody ImportGuestsRequest request) {
        return guestService.importGuests(request);
    }
}
