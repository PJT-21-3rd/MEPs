package org.meps.user.dto;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Setter @Getter
@ToString(exclude = "password")
@EqualsAndHashCode(of = "userId")
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Integer userId;

    private String email;

    private String password;

    private LocalDateTime createdAt;
}