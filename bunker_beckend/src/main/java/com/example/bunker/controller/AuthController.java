package com.example.bunker.controller;


import com.example.bunker.dto.User.UserRequestLogin;
import com.example.bunker.dto.User.UserRequestRegister;
import com.example.bunker.dto.User.UserResponse;
import com.example.bunker.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    public record GoogleAuthRequest(String token) {}

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRequestRegister user){
        authService.registerUser(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserRequestLogin user){
        UserResponse userResponse =authService.loginUser(user);
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping("/register/google")
    public ResponseEntity<?> registerGoogle(@RequestBody @Valid GoogleAuthRequest request){

        return ResponseEntity.ok().build();
    }
}
