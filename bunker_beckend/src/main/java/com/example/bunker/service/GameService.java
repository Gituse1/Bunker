package com.example.bunker.service;

import com.example.bunker.dto.Game.DataInStartGame;
import com.example.bunker.dto.Game.DataToUserInStartGame;
import com.example.bunker.dto.ProductDTO;
import com.example.bunker.model.Player;
import com.example.bunker.model.StatusInGame;
import com.example.bunker.model.VisibilityOfCharacteristic;
import com.example.bunker.model.game.GameData;
import com.example.bunker.repository.PlayerRepository;
import com.example.bunker.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

   private final SessionService sessionService;
   private final AuthService authService;

    private final SimpMessagingTemplate messagingTemplate;

   private final PlayerRepository playerRepository;
   private final RoomRepository roomRepository;

    public DataToUserInStartGame continueToGame(Long roomId) {
        String userName = authService.getCurrentUserName();
        ProductDTO  userDto = sessionService.getSession(roomId, userName);

        if(!isProductDtoFullyPopulated(userDto)){
            throw new EntityNotFoundException("User is not fully populated " + userDto);
        }

        Player userPlayer = playerRepository.findByIdPlayerAndCharacteristicAndHeroData(userDto.getPlayerId()).orElseThrow(
                () -> new EntityNotFoundException("Player not found with id: " + userDto.getPlayerId())
        );
        if(!isPlayerFullyPopulated(userPlayer)){
            throw new IllegalArgumentException("Player is not fully populated" + userName);
        }
        GameData gameData =GameData.builder()
                .userName(userName)
                .grown(userPlayer.getCharacter().getGrown())
                .figure(userPlayer.getCharacter().getFigure().toString())

                .heroArtifact(userPlayer.getArtifactHeroCatalog().getName())
                .randomArtifact1(userPlayer.getFirstArtifactRandomCatalog().getName().toString())
                .randomArtifact2(userPlayer.getSecondArtifactRandomCatalog().getName().toString())

                .physicalCondition(userPlayer.getCharacter().getPhysicalCondition().toString())
                .psychologicalState(userPlayer.getCharacter().getPsyhologicalState().toString())

                .profession(userPlayer.getHero().getProfession().toString())
                .stateOfHealth(userPlayer.getCharacter().getStateOfHealth().toString())
                .skill(userPlayer.getHero().getSkills().toString())
                .rase(userPlayer.getHero().getRace().toString())
                .hobby(userPlayer.getHero().getHobby().toString())
                .secret(userPlayer.getCharacter().getSecret().toString())
                .build();

        VisibilityOfCharacteristic visibility = userPlayer.getVisibilityOfCharacteristic();


        String roomCode = roomRepository.findCodeToConnectById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room code not found"));

        messagingTemplate.convertAndSend(
                "/topic/game/" + roomCode,
                DataInStartGame.builder()
                        .gameData(gameData)
                        .visibilityOfCharacteristic(visibility)
                        .build());
        sessionService.updateSession(roomId,userName,dto ->{
            dto.setStatusInGame(StatusInGame.IN_GAME);
        });

        List<ProductDTO> productDTOS = sessionService.getAllSessionByRoomId(roomId);

        List<ProductDTO> productDto =productDTOS.stream()
                .filter(o ->o.getStatusInGame().equals(StatusInGame.IN_GAME))
                .toList();

        List<GameData> gameDataList = getGameDataForMultipleUsers(productDto,userName);

        messagingTemplate.convertAndSendToUser(userName,userName,gameDataList);


        return DataToUserInStartGame.builder()
                .otherUserGameDataList(gameDataList)
                .userGameData(gameData)
                .build();
    }


    private boolean isPlayerFullyPopulated(Player player) {
        return Stream.of(
                player.getId(),
                player.getHero(),
                player.getUser(),
                player.getVisibilityOfCharacteristic(),
                player.getCharacter(),
                player.getArtifactHeroCatalog(),
                player.getFirstArtifactRandomCatalog(),
                player.getSecondArtifactRandomCatalog(),
                player.getCreatedAt(),
                player.getStatus(),
                player.getEffect()
        ).noneMatch(Objects::isNull);
    }
    private boolean isProductDtoFullyPopulated(ProductDTO dto) {


        return Stream.of(
                dto.getHeroId(),
                dto.getPlayerId(),
                dto.getCharacterId(),
                dto.getArtifactHeroId(),
                dto.getArtifactRand1Id(),
                dto.getArtifactRand2Id(),
                dto.getVisibilityId(),
                dto.getStatusInGame()
        ).noneMatch(Objects::isNull);
    }

    private List<GameData> getGameDataForMultipleUsers(List<ProductDTO> productDtoList, String userName) {
        if (productDtoList == null || productDtoList.isEmpty()) {
            return List.of();
        }

        List<Long> playerIds = productDtoList.stream()
                .map(ProductDTO::getPlayerId)
                .collect(Collectors.toList());

        List<Player> playersFromDb = playerRepository.findAllByIdsWithRelations(playerIds);


        Map<Long, Player> playerMap = playersFromDb.stream()
                .collect(Collectors.toMap(Player::getId, player -> player));

        return productDtoList.stream()
                .map(dto -> {
                    // Шукаємо гравця в завантаженій карті
                    Player userPlayer = playerMap.get(dto.getPlayerId());

                    if (userPlayer == null) {
                        throw new EntityNotFoundException("Player not found with id: " + dto.getPlayerId());
                    }


                    if (!isPlayerFullyPopulated(userPlayer)) {
                        throw new IllegalArgumentException("Player is not fully populated: " + userName);
                    }


                    return GameData.builder()
                            .userName(dto.getUserName())
                            .grown(userPlayer.getCharacter().getGrown())
                            .figure(userPlayer.getCharacter().getFigure().toString())

                            .heroArtifact(userPlayer.getArtifactHeroCatalog().getName())
                            .randomArtifact1(userPlayer.getFirstArtifactRandomCatalog().getName())
                            .randomArtifact2(userPlayer.getSecondArtifactRandomCatalog().getName())

                            .physicalCondition(userPlayer.getCharacter().getPhysicalCondition().toString())
                            .psychologicalState(userPlayer.getCharacter().getPsyhologicalState().toString())

                            .profession(userPlayer.getHero().getProfession().toString())
                            .stateOfHealth(userPlayer.getCharacter().getStateOfHealth().toString())
                            .skill(userPlayer.getHero().getSkills().toString())
                            .rase(userPlayer.getHero().getRace().toString())
                            .hobby(userPlayer.getHero().getHobby().toString())
                            .secret(userPlayer.getCharacter().getSecret().toString())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
