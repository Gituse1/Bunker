package com.example.bunker.dto.Player;

import com.example.bunker.model.characteristic.Characteristic;

public record PlayerCharacteristicUpdateDto(
        Characteristic characteristicName,
        String newValue
) {
}
