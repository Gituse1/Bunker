package com.example.bunker.model;

import com.example.bunker.model.characteristic.*;
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
@Table(name ="characteristic")
public class CharacteristicPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private double grown;

    @Enumerated(EnumType.STRING)
    @Column(name = "state_of_health")
    private StateOfHealth stateOfHealth;

    @Enumerated(EnumType.STRING)
    private Figure figure;

    @Enumerated(EnumType.STRING)
    @Column(name = "physical_condition")
    private PhysicalCondition physicalCondition;

    @Enumerated(EnumType.STRING)
    @Column(name ="psyhological_state")
    private PsychologicalState psyhologicalState;

    @Enumerated(EnumType.STRING)

    private Secret secret;

}
