package com.example.bunker.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@Setter
@Getter
@Entity
@Table
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_name")
    private String name;

    @Column(unique = true)
    private String password;

    @Column(unique = true)
    private String email;

    @Column(nullable = true)
    private String googleId;

    private AuthProvider authProvider;

    @Column(name = "create_date")
    LocalDateTime createDate;

    @Column(name = "update_date")
    LocalDateTime updateDate;

    @Column(name ="last_visit")
    private LocalDateTime lastVisit;
}
