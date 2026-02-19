package com.dvgs.ration.api;

import com.dvgs.ration.api.RationDtos.Location;
import com.dvgs.ration.api.RationDtos.Member;
import com.dvgs.ration.api.RationDtos.RationCardResponse;
import com.dvgs.ration.external.ExternalRationApiClient;
import com.dvgs.ration.external.ExternalRationApiClient.CardNotFoundException;
import com.dvgs.ration.external.RationCardExternalDto;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RationReadService {

    private final ExternalRationApiClient client;

    public RationReadService(ExternalRationApiClient client) {
        this.client = client;
    }

    public Mono<RationCardResponse> getCard(String cardNo, Authentication auth) {
        boolean isOfficial = hasAnyRole(auth, "ROLE_OFFICIAL", "ROLE_ADMIN", "ROLE_SUPER_ADMIN");

        return client.getCardByNo(cardNo)
                .map(dto -> isOfficial ? toOfficialResponse(dto) : toCitizenResponse(dto))
                .onErrorResume(CardNotFoundException.class, ex -> {
                    if (isOfficial) {
                        return Mono.error(ex);
                    }
                    return Mono.just(new RationCardResponse(false, cardNo, null, null, null, null, List.of()));
                });
    }

    private RationCardResponse toCitizenResponse(RationCardExternalDto dto) {
        int count = dto.members() == null ? 0 : dto.members().size();
        return new RationCardResponse(true, dto.cardNo(), dto.cardType(), dto.status(), count, null, List.of());
    }

    private RationCardResponse toOfficialResponse(RationCardExternalDto dto) {
        Location location = dto.location() == null ? null : new Location(
                dto.location().district(), dto.location().anchal(), dto.location().mauza());

        List<Member> members = dto.members() == null ? List.of() : dto.members().stream()
                .map(m -> new Member(m.name(), m.relation(), m.dob()))
                .toList();

        return new RationCardResponse(true, dto.cardNo(), dto.cardType(), dto.status(), members.size(), location, members);
    }

    private static boolean hasAnyRole(Authentication auth, String... roles) {
        if (auth == null) return false;
        for (GrantedAuthority a : auth.getAuthorities()) {
            String v = a.getAuthority();
            for (String r : roles) {
                if (r.equals(v)) return true;
            }
        }
        return false;
    }

}
