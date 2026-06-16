package com.example.bunker.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name ="heroes")
public class Hero {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Profession profession;

    @Column(unique = true)
    @Enumerated(EnumType.STRING)
    private Rase rase;

    private String hobby;

    @Column(columnDefinition = "text[]")
    private String[] skills;
}
