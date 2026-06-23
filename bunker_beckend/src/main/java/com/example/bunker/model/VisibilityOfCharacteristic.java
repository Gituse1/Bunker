package com.example.bunker.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class VisibilityOfCharacteristic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="artefact_hero_visible")
    private boolean artefactHeroIsVisible =false;

    @Column(name="artefact_random1_visible")
    private boolean artefactRandom1IsVisible = false;

    @Column(name="artefact_random2_visible")
    private boolean artefactRandom2IsVisible =false;

    @Column(name="state_of_health_visible")
    private boolean stateOfHealthIsVisible =true;

    @Column(name="grown_visible")
    private boolean grownIsVisible =false;

    @Column(name="figure_visible")
    private boolean figureIsVisible=false;

    @Column(name="physical_condition_visible")
    private boolean physicalConditionIsVisible=false;

    @Column(name="psyhological_state_visible")
    private boolean psyhologicalStateIsVisible =false;

    @Column(name="secrets_visible")
    private boolean secretsIsVisible =false;

    @Column(name = "profession_visible")
    private boolean professionIsVisible=true;

    @Column(name = "rase_visible")
    private boolean raseIsVisible =true;

    @Column(name = "skill_visible")
    private boolean skillsIsVisible=true;

    @Column(name = "hobby_visible")
    private boolean hobbyIsVisible=false;
}
