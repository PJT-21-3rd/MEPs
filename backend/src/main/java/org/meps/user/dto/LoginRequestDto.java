package org.meps.user.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * 로그인 요청
 *
 * 형식 검증은 하지 않는다. 잘못된 형식이면 어차피 조회에 실패해 401이 나가고,
 * 형식 오류(400)와 인증 실패(401)를 구분하면 계정 존재 여부가 노출될 수 있다.
 * 누락(@NotBlank)만 400으로 처리한다.
 */
@Setter @Getter
@ToString(exclude = "password")
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {

    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}