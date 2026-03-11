package com.wedding.backend.guest.api;

import com.wedding.backend.guest.api.GuestDtos.ConfirmGuestsRequest;
import com.wedding.backend.guest.api.GuestDtos.GuestPageResponse;
import com.wedding.backend.guest.api.GuestDtos.GuestResponse;
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

    @PostMapping("/confirm")
    @ResponseStatus(HttpStatus.CREATED)
    public List<GuestResponse> confirmGuests(@Valid @RequestBody ConfirmGuestsRequest request) {
        return guestService.confirmGuests(request);
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
}

