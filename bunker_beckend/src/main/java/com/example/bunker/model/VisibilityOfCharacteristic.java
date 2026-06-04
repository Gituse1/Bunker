package com.example.bunker.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class VisibilityOfCharacteristic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name="artefact_hero_visible")
    private boolean artefactHeroIsVisible =false;

    @Column(name="artefact_random1_visible")
    private boolean artefactRandom1IsVisible = false;

    @Column(name="artefact_random2_visible")
    private boolean artefactRandom2IsVisible =false;

    @Column(name="state_of_health_visible")
    private boolean stateOfHealthIsVisible =false;

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
}
