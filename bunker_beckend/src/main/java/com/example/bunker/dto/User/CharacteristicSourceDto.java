package com.example.bunker.dto.User;

import com.example.bunker.projection.CharacteristicSource;
import lombok.Builder;

@Builder

public record CharacteristicSourceDto(
        CharacteristicSource characteristicSource1,
        CharacteristicSource characteristicSource2
) {
}
