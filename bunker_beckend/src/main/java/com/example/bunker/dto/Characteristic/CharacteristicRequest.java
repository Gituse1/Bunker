package com.example.bunker.dto.Characteristic;


import com.example.bunker.model.characteristic.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CharacteristicRequest {

    private StateOfHealth state_of_health;
    private Figure figure;
    private PhysicalCondition physical_condition;
    private PsychologicalState psyhologicalState;
    private Secret secret;
    private double grown;


}
