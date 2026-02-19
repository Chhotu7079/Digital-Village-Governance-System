package com.dvgs.land.api;

import com.dvgs.land.api.LandDtos.LandRecordResponse;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/land")
public class LandController {

    private final LandReadService service;

    public LandController(LandReadService service) {
        this.service = service;
    }

    @GetMapping("/records/search")
    public Mono<LandRecordResponse> search(
            @RequestParam @NotBlank String district,
            @RequestParam @NotBlank String anchal,
            @RequestParam @NotBlank String mauza,
            @RequestParam @NotBlank String khataNo,
            @RequestParam @NotBlank String khesraNo,
            Authentication auth
    ) {
        return service.search(new LandDtos.LandSearchRequest(district, anchal, mauza, khataNo, khesraNo), auth);
    }
}
