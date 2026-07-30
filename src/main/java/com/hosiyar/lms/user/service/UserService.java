package com.hosiyar.lms.user.service;

import com.hosiyar.lms.common.exception.BadRequestException;
import com.hosiyar.lms.user.dto.RegisterRequest;
import com.hosiyar.lms.user.dto.UserResponse;
import com.hosiyar.lms.user.entity.Role;
import com.hosiyar.lms.user.entity.User;
import com.hosiyar.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("An account with this email already exists");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.STUDENT);

        User saved;
        try {
            saved = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            // Closes the race condition the existsByEmail() check above can't:
            // two concurrent registrations with the same email can both pass
            // that check before either commits. The database-level unique
            // constraint (see the Flyway migration) is the real guarantee -
            // this just turns its violation into the same clean error
            // instead of a raw SQL exception leaking out.
            throw new BadRequestException("An account with this email already exists");
        }

        return new UserResponse(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getRole(),
                saved.getCreatedAt()
        );
    }
}
