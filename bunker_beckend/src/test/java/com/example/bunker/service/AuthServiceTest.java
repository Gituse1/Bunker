package com.example.bunker.service;

import com.example.bunker.dto.User.UserRequestLogin;
import com.example.bunker.dto.User.UserRequestRegister;
import com.example.bunker.dto.User.UserResponse;
import com.example.bunker.model.User;
import com.example.bunker.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Nested
    class RegisterUser {

        @Test
        public void shouldRegisterUserSuccessfully() {
            UserRequestRegister request = getTestRegisterRequest();
            User user = getTestUser();
            String encodedPassword = "encodedPassword";
            String token = "jwt-token";

            when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);

            // Якщо toUser викликає статичний метод для створення, ми перехоплюємо об'єкт перед збереженням
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(jwtService.generateToken(any(User.class))).thenReturn(token);

            UserResponse response = authService.registerUser(request);

            assertNotNull(response);
            assertEquals(token, response.getToken());

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(1)).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertNotNull(savedUser.getCreateDate());
            assertNotNull(savedUser.getLastVisit());
        }

        @Test
        public void shouldThrowExceptionWhenUsernameAlreadyExists() {
            UserRequestRegister request = getTestRegisterRequest();

            when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> authService.registerUser(request)
            );

            assertEquals("Username вже зайнятий " + request.getUsername(), exception.getMessage());
            verify(userRepository, never()).existsByEmail(anyString());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        public void shouldThrowExceptionWhenEmailAlreadyExists() {
            UserRequestRegister request = getTestRegisterRequest();

            when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> authService.registerUser(request)
            );

            assertEquals("Email вже зайнятий " + request.getEmail(), exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    class LoginUser {

        @Test
        public void shouldLoginUserSuccessfully() {
            UserRequestLogin request = getTestLoginRequest();
            User user = getTestUser();
            String token = "jwt-token";

            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(jwtService.generateToken(user)).thenReturn(token);

            UserResponse response = authService.loginUser(request);

            assertNotNull(response);
            assertEquals(token, response.getToken());

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(1)).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertNotNull(savedUser.getLastVisit());
        }

        @Test
        public void shouldThrowEntityNotFoundWhenUserDoesNotExist() {
            UserRequestLogin request = getTestLoginRequest();

            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> authService.loginUser(request)
            );

            assertEquals("Користувача не знайдено " + request.getEmail(), exception.getMessage());
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        public void shouldThrowBadCredentialsWhenPasswordDoesNotMatch() {
            UserRequestLogin request = getTestLoginRequest();
            User user = getTestUser();

            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

            BadCredentialsException exception = assertThrows(
                    BadCredentialsException.class,
                    () -> authService.loginUser(request)
            );

            assertEquals("Невірний пароль " + request.getPassword(), exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
            verify(jwtService, never()).generateToken(any(User.class));
        }
    }

    @Nested
    class GetCurrentUserName {

        @Test
        public void shouldReturnCurrentUserName() {
            String expectedUserName = "testUser";

            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(expectedUserName);

            SecurityContextHolder.setContext(securityContext);

            String result = authService.getCurrentUserName();

            assertEquals(expectedUserName, result);

            SecurityContextHolder.clearContext();
        }
    }

    private UserRequestRegister getTestRegisterRequest() {
        // Припускаємо, що DTO має builder або стандартні методи встановлення значень
        return UserRequestRegister.builder()
                .username("user1")
                .email("testEmail@gmail.com")
                .password("rawPassword")
                .build();
    }

    private UserRequestLogin getTestLoginRequest() {
        return UserRequestLogin.builder()
                .email("testEmail@gmail.com")
                .password("rawPassword")
                .build();
    }

    private User getTestUser() {
        return User.builder()
                .id(1L)
                .username("user1")
                .email("testEmail@gmail.com")
                .password("encodedPassword")
                .createDate(LocalDateTime.now().minusDays(1))
                .lastVisit(LocalDateTime.now().minusDays(1))
                .build();
    }
}