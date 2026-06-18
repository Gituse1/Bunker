package com.example.bunker.dto.User;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestLogin {
    @NotBlank private String email;
    @NotBlank private String password;
}