package com.example.bunker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Effect {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_protected")
    private boolean isProtected =false;

    @Column(name = "is_stunned")
    private boolean isStunned =false;

    @Column(name = "time_of_protection")
    private Integer timeOfProtection =0;

    @Column(name = "time_of_stun")
    private Integer timeOfStun =0;
}
