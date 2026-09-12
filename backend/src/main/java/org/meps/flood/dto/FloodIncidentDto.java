package org.meps.flood.dto;

import lombok.*;

@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FloodIncidentDto {
    private String year;
    private int grade;
    private String sggCd;
    private String cause;
}
