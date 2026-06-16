package com.example.bunker.service;

import com.example.bunker.model.CharacteristicPlayer;
import com.example.bunker.model.characteristic.*;
import com.example.bunker.repository.CharacteristicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;


@Service
@RequiredArgsConstructor
public class CharacteristicService {

    private final CharacteristicRepository characteristicRepository;
    private final SessionService sessionService;
    private final AuthService authService;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final List<Double> GROWTH = List.of(168.0, 175.0, 178.0, 182.0, 190.0);

    @Transactional
    public CharacteristicPlayer createCharacteristic(Long roomId) {
        StateOfHealth stateOfHealth = getRandom(List.of(StateOfHealth.values()));
        PhysicalCondition physicalCondition = getBalancedPhysicalCondition(stateOfHealth);

        CharacteristicPlayer characteristicPlayer= CharacteristicPlayer.builder()
                .state_of_health(stateOfHealth)
                .grown(getRandom(GROWTH))
                .figure(getRandom(List.of(Figure.values())))
                .physical_condition(physicalCondition)
                .psyhologicalState(getRandom(List.of(PsychologicalState.values())))
                .secret(getRandom(List.of(Secret.values())))
                .build();

        CharacteristicPlayer characteristicPlayer1 =characteristicRepository.save(characteristicPlayer);

        sessionService.updateSession(roomId, authService.getCurrentUserName(), dto->{
            dto.setCharacterId(characteristicPlayer1.getId());
        });;
        return characteristicPlayer;
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
