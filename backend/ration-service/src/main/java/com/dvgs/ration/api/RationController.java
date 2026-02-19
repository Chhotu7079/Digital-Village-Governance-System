package com.dvgs.ration.api;

import com.dvgs.ration.api.RationDtos.RationCardResponse;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/ration")
public class RationController {

    private final RationReadService service;

    public RationController(RationReadService service) {
        this.service = service;
    }

    @GetMapping("/cards/{cardNo}")
    public Mono<RationCardResponse> getCard(@PathVariable @NotBlank String cardNo, Authentication auth) {
        return service.getCard(cardNo, auth);
    }
}
