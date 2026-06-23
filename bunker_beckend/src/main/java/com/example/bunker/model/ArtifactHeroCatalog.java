package com.example.bunker.model;

import com.example.bunker.projection.CharacteristicSource;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "artifact_hero_catalog")
public class ArtifactHeroCatalog implements CharacteristicSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30)
    private String name;

    @Column(length = 100)
    private String description;

    @Enumerated(EnumType.STRING)
    private ActionTypeArtifact actionType;
}
