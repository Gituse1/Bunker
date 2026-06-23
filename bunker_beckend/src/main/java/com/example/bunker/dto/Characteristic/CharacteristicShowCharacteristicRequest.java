package com.example.bunker.dto.Characteristic;


import lombok.Builder;

@Builder
public record CharacteristicShowCharacteristicRequest(
        String nameCharacteristic,
        String valueCharacteristic
){}
