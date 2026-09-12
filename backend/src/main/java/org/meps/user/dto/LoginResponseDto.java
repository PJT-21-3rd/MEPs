package org.meps.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/** 로그인 응답 — JWT 액세스 토큰 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Setter @Getter
@ToString(exclude = "accessToken")
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    /** JWT 액세스 토큰 */
    private String accessToken;

    /** JWT 리프레시 토큰 */
    private String refreshToken;

    /** 토큰 타입 — 항상 "Bearer" */
    private String tokenType;

    /** 만료 시간(초) */
    private Integer expiresIn;
}