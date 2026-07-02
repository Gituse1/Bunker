package com.example.bunker.dto.Characteristic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CharacteristicArtifactStealing {
    String userName;
    Long  userNewId;

    String targetName;
    Long  targetNewId;

    String characteristicName;
}
