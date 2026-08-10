package org.meps.user.service;

import lombok.RequiredArgsConstructor;
import org.meps.user.dto.SignupRequestDto;
import org.meps.user.dto.UserDto;
import org.meps.user.exception.DuplicateEmailException;
import org.meps.user.exception.PasswordMismatchException;
import org.meps.user.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

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
}