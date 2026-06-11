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
@Table(name ="Players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    @JoinColumn(name ="hero_id")
    private Hero hero;
    @ManyToOne()
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne()
    @JoinColumn(name ="visibility_id")
    private VisibilityOfCharacteristic visibilityOfCharacteristic;

    @ManyToOne()
    @JoinColumn(name = "character_id")
    private CharacteristicPlayer character;

    @ManyToOne()
    @JoinColumn(name ="artifact_hero_id")
    private ArtifactHeroCatalog artifactHeroCatalog;

    @ManyToOne()
    @JoinColumn(name ="artifact_random_id")
    private ArtifactRandomCatalog artifactRandomCatalog;
}
