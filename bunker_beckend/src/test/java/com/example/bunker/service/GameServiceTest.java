package com.example.bunker.service;

import com.example.bunker.dto.Game.DataInStartGame;
import com.example.bunker.dto.Game.DataToUserInStartGame;
import com.example.bunker.dto.ProductDTO;
import com.example.bunker.model.*;
import com.example.bunker.model.characteristic.*;
import com.example.bunker.repository.PlayerRepository;
import com.example.bunker.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private AuthService authService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private GameService gameService;

    @Nested
    class ContinueToGame {

        @Test
        public void shouldContinueToGameSuccessfully() {
            Long roomId = 1L;
            String userName = "testUser";

            ProductDTO validDto = getFullyPopulatedProductDTO(userName);
            Player validPlayer = getFullyPopulatedPlayer();
            String roomCode = "ROOM_CODE";

            when(authService.getCurrentUserName()).thenReturn(userName);
            when(sessionService.getSession(roomId, userName)).thenReturn(validDto);
            when(playerRepository.findByIdPlayerAndCharacteristicAndHeroData(validDto.getPlayerId()))
                    .thenReturn(Optional.of(validPlayer));
            when(roomRepository.findCodeToConnectById(roomId)).thenReturn(Optional.of(roomCode));

            // Мокування для другої частини методу, де дістається список гравців IN_GAME
            ProductDTO inGameDto = getFullyPopulatedProductDTO("otherUser");
            inGameDto.setStatusInGame(StatusInGame.IN_GAME);
            List<ProductDTO> allSessions = List.of(validDto, inGameDto);

            when(sessionService.getAllSessionByRoomId(roomId)).thenReturn(allSessions);

            Player otherPlayer = getFullyPopulatedPlayer();
            otherPlayer.setId(inGameDto.getPlayerId());
            when(playerRepository.findAllByIdsWithRelations(anyList())).thenReturn(List.of(otherPlayer));

            DataToUserInStartGame result = gameService.continueToGame(roomId);

            assertNotNull(result);
            assertNotNull(result.getUserGameData());
            assertNotNull(result.getOtherUserGameDataList());

            // Перевірка оновлення статусу сесії
            verify(sessionService).updateSession(eq(roomId), eq(userName), any());

            // Перевірка відправки повідомлень
            verify(messagingTemplate).convertAndSend(eq("/topic/game/" + roomCode), any(DataInStartGame.class));
            verify(messagingTemplate).convertAndSendToUser(eq(userName), eq(userName), anyList());
        }

        @Test
        public void shouldThrowEntityNotFoundWhenProductDtoIsNotFullyPopulated() {
            Long roomId = 1L;
            String userName = "testUser";

            // DTO з відсутнім heroId
            ProductDTO invalidDto = ProductDTO.builder()
                    .playerId(10L)
                    .characterId(20L)
                    .build();

            when(authService.getCurrentUserName()).thenReturn(userName);
            when(sessionService.getSession(roomId, userName)).thenReturn(invalidDto);

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> gameService.continueToGame(roomId)
            );

            assertTrue(exception.getMessage().contains("User is not fully populated"));
            verify(playerRepository, never()).findByIdPlayerAndCharacteristicAndHeroData(anyLong());
        }

        @Test
        public void shouldThrowEntityNotFoundWhenPlayerNotFoundInDb() {
            Long roomId = 1L;
            String userName = "testUser";

            ProductDTO validDto = getFullyPopulatedProductDTO(userName);

            when(authService.getCurrentUserName()).thenReturn(userName);
            when(sessionService.getSession(roomId, userName)).thenReturn(validDto);
            when(playerRepository.findByIdPlayerAndCharacteristicAndHeroData(validDto.getPlayerId()))
                    .thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> gameService.continueToGame(roomId)
            );

            assertEquals("Player not found with id: " + validDto.getPlayerId(), exception.getMessage());
        }

        @Test
        public void shouldThrowIllegalArgumentWhenPlayerIsNotFullyPopulated() {
            Long roomId = 1L;
            String userName = "testUser";

            ProductDTO validDto = getFullyPopulatedProductDTO(userName);
            Player invalidPlayer = getFullyPopulatedPlayer();
            invalidPlayer.setEffect(null); // Робимо гравця не повністю заповненим

            when(authService.getCurrentUserName()).thenReturn(userName);
            when(sessionService.getSession(roomId, userName)).thenReturn(validDto);
            when(playerRepository.findByIdPlayerAndCharacteristicAndHeroData(validDto.getPlayerId()))
                    .thenReturn(Optional.of(invalidPlayer));

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> gameService.continueToGame(roomId)
            );

            assertEquals("Player is not fully populated" + userName, exception.getMessage());
        }

        @Test
        public void shouldThrowEntityNotFoundWhenRoomCodeNotFound() {
            Long roomId = 1L;
            String userName = "testUser";

            ProductDTO validDto = getFullyPopulatedProductDTO(userName);
            Player validPlayer = getFullyPopulatedPlayer();

            when(authService.getCurrentUserName()).thenReturn(userName);
            when(sessionService.getSession(roomId, userName)).thenReturn(validDto);
            when(playerRepository.findByIdPlayerAndCharacteristicAndHeroData(validDto.getPlayerId()))
                    .thenReturn(Optional.of(validPlayer));
            when(roomRepository.findCodeToConnectById(roomId)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> gameService.continueToGame(roomId)
            );

            assertEquals("Room code not found", exception.getMessage());
        }
    }

    private ProductDTO getFullyPopulatedProductDTO(String userName) {
        return ProductDTO.builder()
                .userName(userName)
                .heroId(1L)
                .playerId(2L)
                .characterId(3L)
                .artifactHeroId(4L)
                .artifactRand1Id(5L)
                .artifactRand2Id(6L)
                .visibilityId(7L)
                .statusInGame(StatusInGame.PREPARATION_FOR_THE_GAME)
                .build();
    }

    private Player getFullyPopulatedPlayer() {
        Hero hero = Hero.builder()
                .profession(Profession.DOCTOR)
                .skills("Healing")
                .race(Race.HUMAN)
                .hobby(Hobby.READING)
                .build();

        CharacteristicPlayer characteristic = CharacteristicPlayer.builder()
                .grown(180.0)
                .figure(Figure.ATHLETIC)
                .physicalCondition(PhysicalCondition.STRONG)
                .psyhologicalState(PsychologicalState.STABLE)
                .stateOfHealth(StateOfHealth.HEALTHY)
                .secret(Secret.NO_SECRET)
                .build();

        ArtifactHeroCatalog artifactHero = ArtifactHeroCatalog.builder().name("Medkit").build();
        ArtifactRandomCatalog artifactRand1 = ArtifactRandomCatalog.builder().name("Flashlight").build();
        ArtifactRandomCatalog artifactRand2 = ArtifactRandomCatalog.builder().name("Radio").build();

        return Player.builder()
                .id(2L)
                .user(User.builder().build())
                .visibilityOfCharacteristic(VisibilityOfCharacteristic.builder().build())
                .hero(hero)
                .character(characteristic)
                .artifactHeroCatalog(artifactHero)
                .firstArtifactRandomCatalog(artifactRand1)
                .secondArtifactRandomCatalog(artifactRand2)
                .createdAt(LocalDateTime.now())
                .status(StatusInGame.PREPARATION_FOR_THE_GAME)
                .effect(Effect.builder().build())
                .build();
    }
}