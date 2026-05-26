package com.example.bunker.dto.User;

import com.example.bunker.model.User;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestRegister extends UserRequestLogin {

    @NotBlank private String email;


    static public User from(UserRequestRegister userRequestRegister){
        return User.builder()
                .email(userRequestRegister.getEmail())
                .password(userRequestRegister.getPassword())
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .lastVisit(LocalDateTime.now())
                .build();
    }
}
