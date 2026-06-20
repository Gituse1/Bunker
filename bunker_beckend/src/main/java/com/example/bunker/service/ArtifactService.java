package com.example.bunker.service;

import com.example.bunker.dto.Player.GameEventDto;
import com.example.bunker.dto.Player.PlayerCharacteristicUpdateDto;
import com.example.bunker.dto.ProductDTO;
import com.example.bunker.dto.ProductDTORequest;
import com.example.bunker.model.ActionTypeArtifact;
import com.example.bunker.model.CharacteristicPlayer;
import com.example.bunker.model.characteristic.Characteristic;
import com.example.bunker.model.characteristic.Figure;
import com.example.bunker.model.characteristic.PhysicalCondition;
import com.example.bunker.model.characteristic.PsychologicalState;
import com.example.bunker.repository.CharacteristicRepository;
import com.example.bunker.repository.PlayerRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArtifactService {
    private final AuthService authService;
    private final SessionService sessionService;

    private final CharacteristicRepository characteristicRepository;
    private final PlayerRepository playerRepository;

    private final SimpMessagingTemplate messagingTemplate;


    public void usePurification(Long artifactId, Long roomId, Long playerId, Characteristic characteristic) {

        ProductDTORequest productDTORequest =auditData(roomId,playerId);
        ProductDTO firstUser =productDTORequest.getProductDTO1();
        ProductDTO secondUser =productDTORequest.getProductDTO2();
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
            characteristicRepository.save(characteristicPlayer);
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

    public void useCurse(Long artifactId, Long roomId, Long targetPlayerId, Characteristic characteristic){

        ProductDTORequest productDTORequest =auditData(roomId,targetPlayerId);
        ProductDTO firstUser =productDTORequest.getProductDTO1();
        ProductDTO secondUser =productDTORequest.getProductDTO2();
        if (firstUser.getArtifactRand1Id().equals(artifactId) || firstUser.getArtifactRand2Id().equals(artifactId)) {
            CharacteristicPlayer characteristicPlayer = characteristicRepository.findById(secondUser.getCharacterId())
                    .orElseThrow(() -> new EntityNotFoundException("In Redis characteristicId is not correct"));

            switch (characteristic) {
                case PHYSICAL_CONDITION -> {
                    switch (characteristicPlayer.getPhysicalCondition()) {
                        case STRONG ->
                                characteristicPlayer.setPhysicalCondition(PhysicalCondition.DISABILITY);
                        case DISABILITY ->
                                characteristicPlayer.setPhysicalCondition(PhysicalCondition.WOUNDED);
                        case WOUNDED ->
                                characteristicPlayer.setPhysicalCondition(PhysicalCondition.WEAK);
                        case WEAK ->
                                throw new NoResultException("There is nothing weaker than weak");
                        default ->
                                throw new IllegalArgumentException("Invalid physical characteristic value");
                    }
                }
                case PSYCHOLOGICAL_STATE -> {
                    switch (characteristicPlayer.getPsyhologicalState()) {
                        case HAPPY ->
                                characteristicPlayer.setPsyhologicalState(PsychologicalState.STABLE);
                        case STABLE ->
                                characteristicPlayer.setPsyhologicalState(PsychologicalState.UNSTABLE);
                        case AGGRESSIVE, UNSTABLE, PARANOID ->
                                characteristicPlayer.setPsyhologicalState(PsychologicalState.BIPOLAR);
                        case BIPOLAR ->
                                throw new NoResultException("There is nothing worse than bipolar state");
                        default ->
                                throw new IllegalArgumentException("Invalid psychological characteristic value");
                    }
                }
                case FIGURE -> {
                    switch (characteristicPlayer.getFigure()) {
                        case MUTATED ->
                                characteristicPlayer.setFigure(Figure.STRONG_BUILD);
                        case STRONG_BUILD ->
                                characteristicPlayer.setFigure(Figure.ATHLETIC);
                        case ATHLETIC ->
                            characteristicPlayer.setFigure(Figure.SLIM);
                        case SLIM ->
                                characteristicPlayer.setFigure(Figure.THIN);
                        case THIN ->
                                throw new NoResultException("There is nothing thinner than thin");
                        default ->
                                throw new IllegalArgumentException("Invalid figure characteristic value");
                    }
                }
            }
            characteristicPlayer = characteristicRepository.save(characteristicPlayer);

            PlayerCharacteristicUpdateDto updateDto = new PlayerCharacteristicUpdateDto(
                    characteristic,
                    characteristicPlayer.getFigure().toString()
            );

            String targetUserName =playerRepository.findUserNameByPlayerId(targetPlayerId)
                    .orElseThrow(() -> new EntityNotFoundException("User name is not found"));

            messagingTemplate.convertAndSendToUser(
                    targetUserName,
                    "/queue/my-status"
                    , updateDto
            );
            GameEventDto gameEventDto = new GameEventDto(
                    "Гравець " + firstUser + " поранив гравця " + targetUserName,
                    targetUserName,
                    ActionTypeArtifact.CURSE,
                    characteristic,
                    characteristicPlayer.getFigure().toString()
            );

        }
    }

    private ProductDTORequest auditData(Long roomId,Long playerId){
        String firstUserName = authService.getCurrentUserName();

        ProductDTO firstUser = sessionService.getSession(roomId,firstUserName);
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
        return ProductDTORequest.builder()
                .productDTO1(firstUser)
                .productDTO2(secondUser)
                .build();
    }


    @Transactional
    public void degradePlayerCharacteristic(Long targetPlayerId,Long roomId, String characteristic, Long artifactId) {



            // Б. Шлемо всій кімнаті повідомлення, що Олег тепер поранений
            GameEventDto publicDto = new GameEventDto(
                    "Гравець " + firstUser.getUsername() + " поранив гравця " + targetPlayer.getUsername(),
                    targetPlayer.getId(),
                    "DEBUFF"
            );
            // Надсилаємо в загальний топік кімнати (наприклад, /topic/room.123456)
            messagingTemplate.convertAndSend(
                    "/topic/room." + targetPlayer.getRoomCode(),
                    publicDto
            );
        }
    }
}

