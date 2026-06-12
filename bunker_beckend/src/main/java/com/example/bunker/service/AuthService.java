package com.example.bunker.service;

import com.example.bunker.dto.User.UserRequestLogin;
import com.example.bunker.dto.User.UserRequestRegister;
import com.example.bunker.dto.User.UserResponse;
import com.example.bunker.model.User;
import com.example.bunker.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public UserResponse registerUser(UserRequestRegister dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username вже зайнятий");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email вже зайнятий");
        }
        User user = UserRequestRegister.toUser(dto, passwordEncoder.encode(dto.getPassword()));
        user.setCreateDate(LocalDateTime.now());
        user.setLastVisit(LocalDateTime.now());
        userRepository.save(user);
        return new UserResponse(jwtService.generateToken(user));

    }

    public UserResponse loginUser(UserRequestLogin dto) {
        User user = userRepository.findByEmail(dto.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Користувача не знайдено"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Невірний пароль");
        }

        user.setLastVisit(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new UserResponse(token);
    }

    public String getCurrentUserName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}