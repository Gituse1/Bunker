package com.example.bunker.dto.Artifact;

import com.example.bunker.model.characteristic.Characteristic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataToUsingArtifactRequest {
    private Long artifactId;
    private Long targetPlayerId;
    private Characteristic characteristicToChange;
}
