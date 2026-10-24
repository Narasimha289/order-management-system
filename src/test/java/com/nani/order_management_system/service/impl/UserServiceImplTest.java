
package com.nani.order_management_system.service.impl;

import com.nani.order_management_system.dto.LoginRequest;
import com.nani.order_management_system.dto.LoginResponse;
import com.nani.order_management_system.dto.UserRequest;
import com.nani.order_management_system.dto.UserResponse;
import com.nani.order_management_system.entity.User;
import com.nani.order_management_system.exception.EmailAlreadyExistsException;
import com.nani.order_management_system.exception.InvalidCredentialsException;
import com.nani.order_management_system.repository.UserRepository;
import com.nani.order_management_system.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRequest userRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        userRequest = new UserRequest();
        userRequest.setName("Test User");
        userRequest.setEmail("test@example.com");
        userRequest.setPassword("Password123!");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("Password123!");

        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setRole("USER");
    }

    @Test
    void registerUserShouldReturnUserResponse() {
        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("Password123!"))
                .thenReturn("encodedPassword");
        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        UserResponse response = userService.registerUser(userRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test User", response.getName());
        assertEquals("test@example.com", response.getEmail());

        verify(passwordEncoder).encode("Password123!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUserShouldThrowWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.registerUser(userRequest)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginShouldReturnTokenWhenCredentialsAreValid() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "encodedPassword"))
                .thenReturn(true);
        when(jwtService.generateToken("test@example.com"))
                .thenReturn("test-jwt-token");

        LoginResponse response = userService.login(loginRequest);

        assertNotNull(response);
        assertEquals("test-jwt-token", response.getToken());
    }

    @Test
    void loginShouldThrowWhenEmailDoesNotExist() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.login(loginRequest)
        );

        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void loginShouldThrowWhenPasswordIsIncorrect() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "encodedPassword"))
                .thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.login(loginRequest)
        );

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void registerUserShouldEncodePasswordBeforeSaving() {
        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("Password123!"))
                .thenReturn("encodedPassword");
        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        userService.registerUser(userRequest);

        verify(passwordEncoder).encode("Password123!");
        verify(userRepository).save(argThat(savedUser ->
                "encodedPassword".equals(savedUser.getPassword())
                        && "USER".equals(savedUser.getRole())
        ));
    }
}
