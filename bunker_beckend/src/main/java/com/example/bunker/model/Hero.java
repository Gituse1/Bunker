package com.example.bunker.model;

import com.example.bunker.projection.CharacteristicSource;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hero implements CharacteristicSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private Profession profession;

    @Column(unique = true)
    @Enumerated(EnumType.STRING)
    private Rase rase;

    private String hobby;

    @Column(columnDefinition = "text[]")
    private String[] skills;
}
