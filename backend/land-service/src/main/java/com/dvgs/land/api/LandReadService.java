package com.dvgs.land.api;

import com.dvgs.land.api.LandDtos.LandRecordResponse;
import com.dvgs.land.api.LandDtos.Owner;
import com.dvgs.land.external.ExternalLandApiClient;
import com.dvgs.land.external.ExternalLandApiClient.LandSearchParams;
import com.dvgs.land.external.ExternalLandApiClient.RecordNotFoundException;
import com.dvgs.land.external.LandRecordExternalDto;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class LandReadService {

    private final ExternalLandApiClient client;

    public LandReadService(ExternalLandApiClient client) {
        this.client = client;
    }

    public Mono<LandRecordResponse> search(LandDtos.LandSearchRequest req, Authentication auth) {
        boolean isOfficial = hasAnyRole(auth, "ROLE_OFFICIAL", "ROLE_ADMIN", "ROLE_SUPER_ADMIN");
        LandSearchParams params = new LandSearchParams(req.district(), req.anchal(), req.mauza(), req.khataNo(), req.khesraNo());

        String cacheKey = String.join("|", req.district(), req.anchal(), req.mauza(), req.khataNo(), req.khesraNo());

        return client.search(cacheKey, params)
                .map(dto -> isOfficial ? toOfficial(dto) : toCitizen(dto))
                .onErrorResume(RecordNotFoundException.class, ex -> {
                    if (isOfficial) return Mono.error(ex);
                    return Mono.just(new LandRecordResponse(false,
                            req.district(), req.anchal(), req.mauza(), req.khataNo(), req.khesraNo(),
                            null, null, 0,
                            null, List.of()));
                });
    }

    private LandRecordResponse toCitizen(LandRecordExternalDto dto) {
        int ownerCount = dto.owners() == null ? 0 : dto.owners().size();
        return new LandRecordResponse(true,
                dto.district(), dto.anchal(), dto.mauza(), dto.khataNo(), dto.khesraNo(),
                dto.area(), dto.unit(), ownerCount,
                null, List.of());
    }

    private LandRecordResponse toOfficial(LandRecordExternalDto dto) {
        List<Owner> owners = dto.owners() == null ? List.of() : dto.owners().stream()
                .map(o -> new Owner(o.name(), o.sharePercent()))
                .toList();
        return new LandRecordResponse(true,
                dto.district(), dto.anchal(), dto.mauza(), dto.khataNo(), dto.khesraNo(),
                dto.area(), dto.unit(), owners.size(),
                dto.jamabandiNo(), owners);
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
