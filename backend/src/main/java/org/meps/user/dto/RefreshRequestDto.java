package org.meps.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter @Setter
@NoArgsConstructor
public class RefreshRequestDto {

    @NotBlank
    private String refreshToken;
}