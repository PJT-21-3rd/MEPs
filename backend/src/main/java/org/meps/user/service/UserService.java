package org.meps.user.service;

import lombok.RequiredArgsConstructor;
import org.meps.building.exception.BuildingNotFoundException;
import org.meps.common.util.SafetyGrade;
import org.meps.user.dto.*;
import org.meps.user.exception.*;
import org.meps.user.jwt.JwtProvider;
import org.meps.user.mapper.RefreshTokenMapper;
import org.meps.user.mapper.UserMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;
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

    @Transactional
    public LoginResponseDto login(LoginRequestDto request) {
        UserDto user = userMapper.findByEmail(request.getEmail());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new LoginFailedException();
        }

        String refreshToken = jwtProvider.createRefreshToken(user.getUserId());
        refreshTokenMapper.upsert(RefreshTokenDto.builder()
                .userId(user.getUserId())
                .token(refreshToken)
                .expiresAt(jwtProvider.getRefreshExpiresAt())
                .build());

        return LoginResponseDto.builder()
                .accessToken(jwtProvider.createToken(user.getUserId()))
                .refreshToken(refreshToken)
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

    /** 마이페이지 찜 목록 조회 */
    public List<SavedBuildingDto> getSavedBuildings(Integer userId) {
        List<SavedBuildingDto> buildings = userMapper.findSavedBuildings(userId);
        for (SavedBuildingDto building : buildings) {
            if (building.getSafetyScore() != null) {
                building.setSafetyGrade(SafetyGrade.fromScore(building.getSafetyScore()).name());
            }
        }
        return buildings;
    }

    /** 액세스 토큰 재발급 — 리프레시 토큰이 DB에 있고 만료되지 않아야 한다 */
    public LoginResponseDto refresh(String refreshToken) {
        RefreshTokenDto stored = refreshTokenMapper.findByToken(refreshToken);
        if (stored == null) {
            throw new InvalidRefreshTokenException("등록되지 않은 리프레시 토큰입니다.");
        }
        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidRefreshTokenException("만료된 리프레시 토큰입니다. userId=" + stored.getUserId());
        }
        // 서명·형식 검증 (무효 시 null 반환)
        if (jwtProvider.getUserId(refreshToken) == null) {
            throw new InvalidRefreshTokenException("서명이 유효하지 않은 리프레시 토큰입니다.");
        }

        return LoginResponseDto.builder()
                .accessToken(jwtProvider.createToken(stored.getUserId()))
                .tokenType("Bearer")
                .expiresIn(jwtProvider.getExpiresInSeconds())
                .build();
    }

    /** 로그아웃 — 리프레시 토큰 삭제 (없어도 성공 처리) */
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenMapper.deleteByToken(refreshToken);
    }
}