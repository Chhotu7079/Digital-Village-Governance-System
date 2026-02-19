package com.dvgs.ration.api;

import java.util.List;

public class RationDtos {

    public record RationCardResponse(
            boolean exists,
            String cardNo,
            String cardType,
            String status,
            Integer memberCount,
            Location location,
            List<Member> members
    ) {}

    public record Location(String district, String anchal, String mauza) {}

    public record Member(String name, String relation, String dob) {}
}
