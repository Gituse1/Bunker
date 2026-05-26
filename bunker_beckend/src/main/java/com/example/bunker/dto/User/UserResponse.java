package com.example.bunker.dto.User;

import com.example.bunker.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private String username;
    private String email;
    private String token;

    static public UserResponse from(User user) {
        return UserResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                //.token(token)
                .build();
    }
}
