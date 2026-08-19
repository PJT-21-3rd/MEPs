package org.meps.user.controller;

import lombok.RequiredArgsConstructor;
import org.meps.common.auth.LoginUser;
import org.meps.user.dto.LoginRequestDto;
import org.meps.user.dto.LoginResponseDto;
import org.meps.user.dto.RefreshRequestDto;
import org.meps.user.dto.SignupRequestDto;
import org.meps.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 회원가입 */
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequestDto request) {
        userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** 로그인 */
    @PostMapping("/login")
    public LoginResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        return userService.login(request);
    }

    /** 회원 탈퇴 */
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(@LoginUser Integer userId) {
        userService.withdraw(userId);
        return ResponseEntity.noContent().build();
    }

    /** 액세스 토큰 재발급 */
    @PostMapping("/refresh")
    public LoginResponseDto refresh(@Valid @RequestBody RefreshRequestDto request) {
        return userService.refresh(request.getRefreshToken());
    }

    /** 로그아웃 */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequestDto request) {
        userService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}