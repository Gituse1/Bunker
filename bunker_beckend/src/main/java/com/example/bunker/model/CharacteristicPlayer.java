package com.example.bunker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name ="characteristics")
public class CharacteristicPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String state_of_health;
    private double grown;
    private String figure;
    private String physical_condition;

    @Column(name ="psyhological_state")
    private String psyhologicalState;
    private String secret;

}
