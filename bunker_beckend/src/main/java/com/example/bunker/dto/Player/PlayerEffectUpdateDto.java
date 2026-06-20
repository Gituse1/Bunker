package com.example.bunker.dto.Player;

public record PlayerEffectUpdateDto(
        String nameOfEffect,
        Integer durationOfEffect,
        boolean newValue
) {
}
