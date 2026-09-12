package org.meps.hjd.dto;

import lombok.*;
import java.util.List;

@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HjdNamesDto {
    private List<HjdNameDto> regions;

    public static HjdNamesDto of(List<HjdNameDto> regions) {
        return HjdNamesDto.builder()
                .regions(regions)
                .build();
    }
}
