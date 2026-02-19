package com.dvgs.landexapi;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/external/land")
public class LandRecordController {

    // key: district|anchal|mauza|khata|khesra
    private final Map<String, LandRecordDto> records = new ConcurrentHashMap<>();

    public LandRecordController() {
        put(new LandRecordDto(
                "Patna", "Danapur", "Mauza-001",
                "15", "221",
                new BigDecimal("0.52"), "ACRE",
                "JMB-1001",
                List.of(
                        new OwnerDto("Ram Kumar", new BigDecimal("50")),
                        new OwnerDto("Sita Devi", new BigDecimal("50"))
                )
        ));

        put(new LandRecordDto(
                "Nalanda", "Biharsharif", "Mauza-021",
                "7", "45",
                new BigDecimal("1.25"), "ACRE",
                "JMB-2009",
                List.of(
                        new OwnerDto("Rakesh Kumar", new BigDecimal("100"))
                )
        ));

        put(new LandRecordDto(
                "Gaya", "Tekari", "Mauza-009",
                "3", "11",
                new BigDecimal("0.18"), "ACRE",
                "JMB-3003",
                List.of(
                        new OwnerDto("Shyam Prasad", new BigDecimal("60")),
                        new OwnerDto("Radha Devi", new BigDecimal("40"))
                )
        ));
    }

    @GetMapping("/records/search")
    public LandRecordDto search(
            @RequestParam @NotBlank String district,
            @RequestParam @NotBlank String anchal,
            @RequestParam @NotBlank String mauza,
            @RequestParam @NotBlank String khataNo,
            @RequestParam @NotBlank String khesraNo
    ) {
        String key = key(district, anchal, mauza, khataNo, khesraNo);
        LandRecordDto dto = records.get(key);
        if (dto == null) {
            throw new RecordNotFoundException(district, anchal, mauza, khataNo, khesraNo);
        }
        return dto;
    }

    private void put(LandRecordDto dto) {
        records.put(key(dto.district(), dto.anchal(), dto.mauza(), dto.khataNo(), dto.khesraNo()), dto);
    }

    private String key(String district, String anchal, String mauza, String khataNo, String khesraNo) {
        return String.join("|",
                district.trim().toLowerCase(),
                anchal.trim().toLowerCase(),
                mauza.trim().toLowerCase(),
                khataNo.trim(),
                khesraNo.trim()
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class RecordNotFoundException extends RuntimeException {
        RecordNotFoundException(String district, String anchal, String mauza, String khataNo, String khesraNo) {
            super("Land record not found for: " + district + "," + anchal + "," + mauza + ", khata=" + khataNo + ", khesra=" + khesraNo);
        }
    }

    public record LandRecordDto(
            String district,
            String anchal,
            String mauza,
            String khataNo,
            String khesraNo,
            BigDecimal area,
            String unit,
            String jamabandiNo,
            List<OwnerDto> owners
    ) {}

    public record OwnerDto(String name, BigDecimal sharePercent) {}
}
