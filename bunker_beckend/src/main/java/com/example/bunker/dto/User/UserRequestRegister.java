package com.example.bunker.dto.User;

import com.example.bunker.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestRegister {

    @NotBlank private String username;
    @NotBlank private String password;
    @Email @NotBlank private String email;

    public static User toUser(UserRequestRegister dto, String encodedPassword) {
        return User.builder()
                .username(dto.getUsername())
                .password(encodedPassword)
                .email(dto.getEmail())
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .lastVisit(LocalDateTime.now())
                .build();
    }
}