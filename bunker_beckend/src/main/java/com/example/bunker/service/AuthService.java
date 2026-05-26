package com.example.bunker.service;


import com.example.bunker.dto.User.UserRequestLogin;
import com.example.bunker.dto.User.UserRequestRegister;
import com.example.bunker.dto.User.UserResponse;
import com.example.bunker.model.User;
import com.example.bunker.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public void registerUser(UserRequestRegister userRequestRegister){
        if(userRepository.existsByEmail(userRequestRegister.getEmail())){
            throw new EntityNotFoundException("User with email already exists");
        }
        User user = UserRequestRegister.from(userRequestRegister);
        userRepository.save(user);
    }

    public UserResponse loginUser(UserRequestLogin user){

        User user1 =userRepository.findByUsername(user.getUsername()).orElseThrow(
                () -> new EntityNotFoundException("User not found"));
        user1.setLastVisit(LocalDateTime.now());
        userRepository.save(user1);

        return UserResponse.from(user1);
    }


}
