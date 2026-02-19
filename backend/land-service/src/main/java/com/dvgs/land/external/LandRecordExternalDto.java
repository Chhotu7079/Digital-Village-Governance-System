package com.dvgs.land.external;

import java.math.BigDecimal;
import java.util.List;

public record LandRecordExternalDto(
        String district,
        String anchal,
        String mauza,
        String khataNo,
        String khesraNo,
        BigDecimal area,
        String unit,
        String jamabandiNo,
        List<Owner> owners
) {
    public record Owner(String name, BigDecimal sharePercent) {}
}
