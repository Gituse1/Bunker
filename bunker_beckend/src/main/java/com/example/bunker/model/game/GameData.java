package com.example.bunker.model.game;

import com.example.bunker.model.Profession;
import com.example.bunker.model.Race;
import com.example.bunker.model.characteristic.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameData {
    private String userName;
    private String profession = Profession.DEFAULT.name();
    private String stateOfHealth = StateOfHealth.DEFAULT.name();
    private String skill = null;
    private String rase = Race.DEFAULT.name();
    private double grown = 150.5;
    private String figure = Figure.DEFAULT.name();
    private String physicalCondition = PhysicalCondition.DEFAULT.name();
    private String psychologicalState = PsychologicalState.DEFAULT.name();
    private String hobby = Hobby.DEFAULT.name();
    private String heroArtifact = "DEFAULT";
    private String randomArtifact1 = "DEFAULT";
    private String randomArtifact2 = "DEFAULT";
    private String secret = Secret.DEFAULT.name();

}
