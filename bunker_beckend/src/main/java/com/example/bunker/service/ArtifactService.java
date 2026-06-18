package com.example.bunker.service;

import com.example.bunker.dto.ProductDTO;
import com.example.bunker.model.ActionTypeArtifact;
import com.example.bunker.model.CharacteristicPlayer;
import com.example.bunker.model.characteristic.Characteristic;
import com.example.bunker.model.characteristic.Figure;
import com.example.bunker.model.characteristic.PhysicalCondition;
import com.example.bunker.model.characteristic.PsychologicalState;
import com.example.bunker.repository.CharacteristicRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArtifactService {
    private final AuthService authService;
    private final SessionService sessionService;

    private final CharacteristicRepository characteristicRepository;


    public void usePurification(Long artifactId, Long roomId, Long playerId, Characteristic characteristic) {
        String userName = authService.getCurrentUserName();

        ProductDTO firstUser = sessionService.getSession(roomId,userName);
        if(firstUser==null){
            throw new IllegalArgumentException("Invalid room id because there is no player in Redis.Room id: "+roomId);
        }

        ProductDTO secondUser = firstUser;
        if(!firstUser.getPlayerId().equals(playerId)) {

            List<ProductDTO> productDTOS = sessionService.getAllSessionByRoomId(roomId);

            List<ProductDTO> productDTOList = productDTOS.stream()
                    .filter(o -> o.getPlayerId().equals(playerId))
                    .toList();
            if (productDTOList.isEmpty()) {
                throw new IllegalArgumentException("Invalid player id because there is no player in Redis. Player id: " + playerId);
            }
            secondUser = productDTOList.get(0);

        }
        if(firstUser.getArtifactRand1Id().equals(artifactId)||firstUser.getArtifactRand2Id().equals(artifactId)){
            CharacteristicPlayer characteristicPlayer = characteristicRepository.findById(secondUser.getCharacterId())
                    .orElseThrow(()-> new EntityNotFoundException("In Redis characteristicId is not correct"));
            switch (characteristic) {
                case PHYSICAL_CONDITION -> {
                    switch (characteristicPlayer.getPhysicalCondition()) {
                        case WEAK:
                            characteristicPlayer.setPhysicalCondition(PhysicalCondition.WOUNDED);
                            break;
                        case WOUNDED:
                            characteristicPlayer.setPhysicalCondition(PhysicalCondition.DISABILITY);
                            break;
                        case DISABILITY:
                            characteristicPlayer.setPhysicalCondition(PhysicalCondition.STRONG);
                            break;
                        case STRONG:
                            throw new NoResultException("There is nothing stronger than strong");
                        default:
                            throw new NumberFormatException("Invalid physical characteristic");

                    }
                }
                case PSYCHOLOGICAL_STATE -> {
                    switch (characteristicPlayer.getPsyhologicalState()) {
                        case HAPPY:
                            throw new NoResultException("There is nothing happier than happy");
                        case STABLE:
                            characteristicPlayer.setPsyhologicalState(PsychologicalState.HAPPY);
                            break;
                        case AGGRESSIVE, UNSTABLE, PARANOID, BIPOLAR:
                            characteristicPlayer.setPsyhologicalState(PsychologicalState.STABLE);
                            break;
                        default:
                            throw new NumberFormatException("Invalid psychological characteristic");
                    }
                }
                case FIGURE -> {
                    switch (characteristicPlayer.getFigure()) {
                        case SLIM:
                            characteristicPlayer.setFigure(Figure.ATHLETIC);
                            break;
                        case THIN:
                            characteristicPlayer.setFigure(Figure.STRONG_BUILD);
                            break;
                        case STRONG_BUILD, ATHLETIC:
                            characteristicPlayer.setFigure(Figure.MUTATED);
                            break;
                        default:
                            throw new NumberFormatException("Invalid figure characteristic");
                    }
                }
            }
        }
    }


    public void underEffect(Long roomId, Long artifactId, ActionTypeArtifact actionTypeArtifact ) {

        String userName = authService.getCurrentUserName();
        ProductDTO firstUser =sessionService.getSession(roomId,userName);
        if(firstUser==null){
            throw new IllegalArgumentException("Data in Redis was broken or room Id is not correct");
        }
        if(firstUser.getArtifactRand1Id().equals(artifactId)||firstUser.getArtifactRand2Id().equals(artifactId)){
            sessionService.updateSession(roomId,userName, dto ->{
                if(actionTypeArtifact.equals(ActionTypeArtifact.STUN)){
                    if(!firstUser.isStunned()){ //false by default
                        dto.setStunned(true);
                    }
                }
                if(actionTypeArtifact.equals(ActionTypeArtifact.PROTECTION)) {
                    if (!firstUser.isProtected()) { //false by default
                        dto.setProtected(true);
                    }
                }
            });
        }
    }
}

