package com.example.bunker.service;

import com.example.bunker.dto.Characteristic.CharacteristicRequest;
import com.example.bunker.model.CharacteristicPlayer;
import com.example.bunker.model.characteristic.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;


@Service
@RequiredArgsConstructor
public class CharacteristicService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final List<Double> GROWTH = List.of(168.0, 175.0, 178.0, 182.0, 190.0);

    public CharacteristicRequest createCharacteristic() {
        StateOfHealth stateOfHealth = getRandom(List.of(StateOfHealth.values()));
        PhysicalCondition physicalCondition = getBalancedPhysicalCondition(stateOfHealth);

        return CharacteristicRequest.builder()
                .state_of_health(stateOfHealth)
                .grown(getRandom(GROWTH))          // ← без List.of()
                .figure(getRandom(List.of(Figure.values())))
                .physical_condition(physicalCondition)
                .psyhologicalState(getRandom(List.of(PsychologicalState.values())))
                .secret(getRandom(List.of(Secret.values())))
                .build();
    }

    private PhysicalCondition getBalancedPhysicalCondition(StateOfHealth stateOfHealth) {
        return switch (stateOfHealth) {
            case HEALTHY -> getRandom(List.of(
                    PhysicalCondition.STRONG,
                    PhysicalCondition.STRONG,
                    PhysicalCondition.WOUNDED));
            case WEAKENED -> getRandom(List.of(
                    PhysicalCondition.WEAK,
                    PhysicalCondition.WOUNDED));
            case PARTIALLY_INFECTED -> getRandom(List.of(
                    PhysicalCondition.WEAK,
                    PhysicalCondition.WOUNDED,
                    PhysicalCondition.DISABILITY));
            case CRITICAL -> getRandom(List.of(
                    PhysicalCondition.DISABILITY,
                    PhysicalCondition.WEAK));
            case WOUNDED -> getRandom(List.of(
                    PhysicalCondition.WOUNDED,
                    PhysicalCondition.WEAK));
        };
    }

    private <T> T getRandom(List<T> list) {
        return list.get(RANDOM.nextInt(list.size()));
    }
}
