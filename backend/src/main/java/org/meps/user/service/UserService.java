package org.meps.user.service;

import lombok.RequiredArgsConstructor;
import org.meps.building.exception.BuildingNotFoundException;
import org.meps.user.dto.LoginRequestDto;
import org.meps.user.dto.LoginResponseDto;
import org.meps.user.dto.SignupRequestDto;
import org.meps.user.dto.UserDto;
import org.meps.user.exception.AlreadySavedException;
import org.meps.user.exception.DuplicateEmailException;
import org.meps.user.exception.LoginFailedException;
import org.meps.user.exception.PasswordMismatchException;
import org.meps.user.jwt.JwtProvider;
import org.meps.user.mapper.UserMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public void signup(SignupRequestDto request) {
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new PasswordMismatchException();
        }
        if (userMapper.countByEmail(request.getEmail()) > 0) {
            throw new DuplicateEmailException(request.getEmail());
        }

        UserDto user = UserDto.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userMapper.insertUser(user);
    }

    public LoginResponseDto login(LoginRequestDto request) {   // ← 메서드 추가
        UserDto user = userMapper.findByEmail(request.getEmail());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new LoginFailedException();
        }

        return LoginResponseDto.builder()
                .accessToken(jwtProvider.createToken(user.getUserId()))
                .tokenType("Bearer")
                .expiresIn(jwtProvider.getExpiresInSeconds())
                .build();
    }

    @Transactional
    public void saveBuilding(Integer userId, String buildingId) {
        if (userMapper.existsSavedBuilding(userId, buildingId)) {
            throw new AlreadySavedException(buildingId);
        }
        try {
            userMapper.insertSavedBuilding(userId, buildingId);
        } catch (DataIntegrityViolationException e) {
            throw new BuildingNotFoundException(buildingId);
        }
        userMapper.incrementSavedCount(buildingId);
    }

    @Transactional
    public void unsaveBuilding(Integer userId, String buildingId) {
        if (userMapper.existsSavedBuilding(userId, buildingId)) {
            userMapper.deleteSavedBuilding(userId, buildingId);
            userMapper.decrementSavedCount(buildingId);
        }
    }

    /** 회원 탈퇴 — 찜 개수 정리 후 회원 삭제 (saved는 CASCADE) */
    @Transactional
    public void withdraw(Integer userId) {
        // 회원 삭제 전에 찜 목록을 확보해야 한다 — CASCADE로 함께 사라지기 때문
        List<String> savedBuildingIds = userMapper.findSavedBuildingIds(userId);
        for (String buildingId : savedBuildingIds) {
            userMapper.decrementSavedCount(buildingId);
        }

        userMapper.deleteUser(userId);
    }
}