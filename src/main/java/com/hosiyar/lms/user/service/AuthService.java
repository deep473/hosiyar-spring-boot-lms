package com.hosiyar.lms.user.service;

import com.hosiyar.lms.common.exception.BadRequestException;
import com.hosiyar.lms.user.dto.AuthResponse;
import com.hosiyar.lms.user.dto.LoginRequest;
import com.hosiyar.lms.user.entity.User;
import com.hosiyar.lms.user.repository.UserRepository;
import com.hosiyar.lms.user.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                // Deliberately the same message for "no such email" and "wrong
                // password" - don't reveal which part was wrong to an attacker.
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken, jwtService.getAccessTokenExpirySeconds());
    }
}
