package com.example.bunker.dto.Player;

import com.example.bunker.model.ActionTypeArtifact;
import com.example.bunker.model.characteristic.Characteristic;

public record GameEventDto(

        String message,
        String playerName,
        ActionTypeArtifact changeType,
        Characteristic characteristic,
        String updatedCharacteristic
) {}
