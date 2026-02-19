package com.dvgs.land.api;

import java.math.BigDecimal;
import java.util.List;

public class LandDtos {

    public record LandRecordResponse(
            boolean exists,
            String district,
            String anchal,
            String mauza,
            String khataNo,
            String khesraNo,
            BigDecimal area,
            String unit,
            Integer ownerCount,
            // Official-only fields (citizen gets null/empty)
            String jamabandiNo,
            List<Owner> owners
    ) {}

    public record Owner(String name, BigDecimal sharePercent) {}

    public record LandSearchRequest(
            String district,
            String anchal,
            String mauza,
            String khataNo,
            String khesraNo
    ) {}
}
