package com.dvgs.rationexapi;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/external/ration")
public class RationCardController {

    private final Map<String, RationCardDto> cards = new ConcurrentHashMap<>();

    public RationCardController() {
        // Seed a few sample Bihar-style ration cards (mock data)
        cards.put("BR-RC-0001", new RationCardDto(
                "BR-RC-0001",
                "PHH",
                "ACTIVE",
                new LocationDto("Patna", "Danapur", "Mauza-001"),
                List.of(
                        new MemberDto("Ram Kumar", "HEAD", "1985-01-10"),
                        new MemberDto("Sita Devi", "SPOUSE", "1988-04-21"),
                        new MemberDto("Aman Kumar", "CHILD", "2012-09-12")
                )
        ));

        cards.put("BR-RC-0002", new RationCardDto(
                "BR-RC-0002",
                "AAY",
                "ACTIVE",
                new LocationDto("Gaya", "Tekari", "Mauza-009"),
                List.of(
                        new MemberDto("Shyam Prasad", "HEAD", "1972-07-05"),
                        new MemberDto("Radha Devi", "SPOUSE", "1976-11-30")
                )
        ));

        // Larger families for memberCount testing
        cards.put("BR-RC-0003", new RationCardDto(
                "BR-RC-0003",
                "PHH",
                "ACTIVE",
                new LocationDto("Nalanda", "Biharsharif", "Mauza-021"),
                List.of(
                        new MemberDto("Rakesh Kumar", "HEAD", "1980-02-14"),
                        new MemberDto("Anita Devi", "SPOUSE", "1984-08-19"),
                        new MemberDto("Rohit Kumar", "CHILD", "2006-06-01"),
                        new MemberDto("Neha Kumari", "CHILD", "2009-01-12"),
                        new MemberDto("Suresh Prasad", "FATHER", "1956-03-03"),
                        new MemberDto("Kamla Devi", "MOTHER", "1959-10-10")
                )
        ));

        cards.put("BR-RC-0004", new RationCardDto(
                "BR-RC-0004",
                "PHH",
                "INACTIVE",
                new LocationDto("Muzaffarpur", "Sakra", "Mauza-114"),
                List.of(
                        new MemberDto("Pooja Kumari", "HEAD", "1991-12-02"),
                        new MemberDto("Aakash Kumar", "SPOUSE", "1989-05-09"),
                        new MemberDto("Priyanshu", "CHILD", "2014-07-07"),
                        new MemberDto("Anshika", "CHILD", "2017-11-23"),
                        new MemberDto("Kiran Devi", "SISTER", "1994-09-15")
                )
        ));
    }

    @GetMapping("/cards/{cardNo}")
    public RationCardDto getCardByNo(@PathVariable @NotBlank String cardNo) {
        RationCardDto dto = cards.get(cardNo);
        if (dto == null) {
            throw new CardNotFoundException(cardNo);
        }
        return dto;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class CardNotFoundException extends RuntimeException {
        CardNotFoundException(String cardNo) {
            super("Ration card not found: " + cardNo);
        }
    }

    public record RationCardDto(
            String cardNo,
            String cardType,
            String status,
            LocationDto location,
            List<MemberDto> members
    ) {}

    public record LocationDto(String district, String anchal, String mauza) {}

    public record MemberDto(String name, String relation, String dob) {}
}
