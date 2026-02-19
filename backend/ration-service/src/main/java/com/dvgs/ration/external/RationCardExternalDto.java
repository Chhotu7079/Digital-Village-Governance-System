package com.dvgs.ration.external;

import java.util.List;

public record RationCardExternalDto(
        String cardNo,
        String cardType,
        String status,
        Location location,
        List<Member> members
) {
    public record Location(String district, String anchal, String mauza) {}

    public record Member(String name, String relation, String dob) {}
}
