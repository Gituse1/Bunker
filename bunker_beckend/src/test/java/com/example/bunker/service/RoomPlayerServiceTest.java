package com.example.bunker.service;

import com.example.bunker.dto.ProductDTO;
import com.example.bunker.dto.Room.RoomDataResponse;
import com.example.bunker.model.*;
import com.example.bunker.projection.PlayerProjection;
import com.example.bunker.repository.EffectRepository;
import com.example.bunker.repository.PlayerRepository;
import com.example.bunker.repository.RoomPlayerRepository;
import com.example.bunker.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomPlayerServiceTest {

    @Mock
    private RoomPlayerRepository roomPlayerRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private EffectRepository effectRepository;
    @Mock
    private PlayerService playerService;
    @Mock
    private SessionService sessionService;
    @Mock
    private AuthService authService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    @Spy
    private RoomPlayerService roomPlayerService;

    @Nested
    class ConnectToGame {

        @Test
        public void shouldConnectExistingPlayerAndChangeStatus() {
            String codeToConnect = "CODE123";
            String userName = "testUser";
            Long roomId = 1L;

            Room room = Room.builder().id(roomId).build();
            Player player = Player.builder().id(10L).status(StatusInGame.WAS_LEFT_EARLIER).build();
            RoomPlayer roomPlayer = RoomPlayer.builder().room(room).player(player).build();

            PlayerProjection projection = getTestPlayerProjection("testUser", 10L);
            List<PlayerProjection> projections = List.of(projection);
            Consumer<ProductDTO> mockConsumer = dto -> {};

            when(authService.getCurrentUserName()).thenReturn(userName);
            when(roomRepository.findRoomByCodeToConnect(codeToConnect)).thenReturn(Optional.of(room));
            when(roomPlayerRepository.findPlayerByRoomPlayerId(roomId, userName)).thenReturn(Optional.of(roomPlayer));

            doReturn(projections).when(roomPlayerService).getUsersNameByRoomId(roomId);
            doReturn(mockConsumer).when(roomPlayerService).preparingRequest(roomPlayer);

            RoomDataResponse response = roomPlayerService.connectToGame(codeToConnect);

            assertNotNull(response);
            assertEquals(roomId, response.getRoomId());
            assertEquals(StatusInGame.PREPARATION_FOR_THE_GAME, roomPlayer.getPlayer().getStatus());
            assertEquals(1, response.getNames().size());
            assertEquals("testUser", response.getNames().get(0));
            assertEquals(10L, response.getIds().get(0));

            verify(roomPlayerRepository).save(roomPlayer);
            verify(sessionService).updateSession(roomId, userName, mockConsumer);
            verify(playerService, never()).createPlayer(anyLong());
        }

        @Test
        public void shouldCreateNewPlayerWhenNotExists() {
            String codeToConnect = "CODE123";
            String userName = "testUser";
            Long roomId = 1L;

            Room room = Room.builder().id(roomId).build();
            Player newPlayer = Player.builder().id(20L).status(StatusInGame.PREPARATION_FOR_THE_GAME).build();
            RoomPlayer newRoomPlayer = RoomPlayer.builder().room(room).build();

            Consumer<ProductDTO> mockConsumer = dto -> {};

            when(authService.getCurrentUserName()).thenReturn(userName);
            when(roomRepository.findRoomByCodeToConnect(codeToConnect)).thenReturn(Optional.of(room));
            when(roomPlayerRepository.findPlayerByRoomPlayerId(roomId, userName)).thenReturn(Optional.empty());

            doReturn(newRoomPlayer).when(roomPlayerService).createRoomPlayer(room);
            when(playerService.createPlayer(roomId)).thenReturn(newPlayer);

            doReturn(Collections.emptyList()).when(roomPlayerService).getUsersNameByRoomId(roomId);
            doReturn(mockConsumer).when(roomPlayerService).preparingRequest(newRoomPlayer);

            RoomDataResponse response = roomPlayerService.connectToGame(codeToConnect);

            assertNotNull(response);
            assertEquals(roomId, response.getRoomId());
            assertEquals(newPlayer, newRoomPlayer.getPlayer());
            assertTrue(response.getNames().isEmpty());

            verify(playerService).createPlayer(roomId);
            verify(roomPlayerRepository).save(newRoomPlayer);
            verify(sessionService).updateSession(roomId, userName, mockConsumer);
        }

        @Test
        public void shouldThrowEntityNotFoundWhenCodeIsInvalid() {
            String codeToConnect = "INVALID_CODE";
            String userName = "testUser";

            when(authService.getCurrentUserName()).thenReturn(userName);
            when(roomRepository.findRoomByCodeToConnect(codeToConnect)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> roomPlayerService.connectToGame(codeToConnect)
            );

            assertEquals("Code is not valid or something went wrong", exception.getMessage());
            verify(roomPlayerRepository, never()).findPlayerByRoomPlayerId(anyLong(), anyString());
            verify(roomPlayerRepository, never()).save(any());
            verify(sessionService, never()).updateSession(anyLong(), anyString(), any());
        }
    }


    @Nested
    class LeaveGame {

        @Test
        public void shouldDeleteSessionAndPlayerWhenStatusIsPreparation() {
            Long roomPlayerId = 1L;
            String userName = "testUser";
            Long roomId = 10L;
            Long playerId = 20L;

            Room room = Room.builder().id(roomId).build();
            Player player = Player.builder().id(playerId).status(StatusInGame.PREPARATION_FOR_THE_GAME).build();
            RoomPlayer roomPlayer = RoomPlayer.builder().id(roomPlayerId).room(room).player(player).build();

            when(authService.getCurrentUserName()).thenReturn(userName);
            when(roomPlayerRepository.findByIdCurrentRoomPlayer(roomPlayerId, userName)).thenReturn(Optional.of(roomPlayer));

            roomPlayerService.leaveGame(roomPlayerId);

            verify(sessionService, times(1)).deleteSession(roomId, userName);
            verify(playerRepository, times(1)).deleteById(playerId);
            verify(roomPlayerRepository, times(1)).deleteById(roomPlayerId);
            verify(roomPlayerRepository, never()).save(any(RoomPlayer.class));
        }

        @Test
        public void shouldUpdateStatusToWasLeftEarlierWhenStatusIsNotPreparation() {
            Long roomPlayerId = 1L;
            String userName = "testUser";

            Player player = Player.builder().id(20L).status(StatusInGame.IN_GAME).build();
            RoomPlayer roomPlayer = RoomPlayer.builder()
                    .id(roomPlayerId)
                    .player(player)
                    .isJoined(true).build();

            when(authService.getCurrentUserName()).thenReturn(userName);
            when(roomPlayerRepository.findByIdCurrentRoomPlayer(roomPlayerId, userName)).thenReturn(Optional.of(roomPlayer));

            roomPlayerService.leaveGame(roomPlayerId);

            assertEquals(StatusInGame.WAS_LEFT_EARLIER, player.getStatus());
            assertFalse(roomPlayer.isJoined());

            verify(roomPlayerRepository, times(1)).save(roomPlayer);
            verify(sessionService, never()).deleteSession(anyLong(), anyString());
            verify(playerRepository, never()).deleteById(anyLong());
            verify(roomPlayerRepository, never()).deleteById(anyLong());
        }

        @Test
        public void shouldThrowIllegalArgumentWhenRoomPlayerDoesNotExist() {
            Long roomPlayerId = 1L;
            String userName = "testUser";

            when(authService.getCurrentUserName()).thenReturn(userName);
            when(roomPlayerRepository.findByIdCurrentRoomPlayer(roomPlayerId, userName)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> roomPlayerService.leaveGame(roomPlayerId)
            );

            assertEquals("Player is not exist", exception.getMessage());

            verify(sessionService, never()).deleteSession(anyLong(), anyString());
            verify(playerRepository, never()).deleteById(anyLong());
            verify(roomPlayerRepository, never()).deleteById(anyLong());
            verify(roomPlayerRepository, never()).save(any(RoomPlayer.class));
        }
    }

    @Nested
    class PlayerExpulsion {

        @Test
        public void shouldExecuteExpulsionSuccessfully() {
            Long roomId = 1L;
            String targetUserName = "targetUser";
            String currentUserName = "currentUser";
            String roomCode = "ROOM_CODE";

            ProductDTO productDTO = ProductDTO.builder().build();

            when(roomRepository.findCodeToConnectById(roomId)).thenReturn(Optional.of(roomCode));
            when(authService.getCurrentUserName()).thenReturn(currentUserName);
            when(sessionService.getSession(roomId, currentUserName)).thenReturn(productDTO);

            // Виклик методу (назва сервісу може відрізнятися залежно від того, в якому класі він знаходиться)
            roomPlayerService.playerExpulsion(roomId, targetUserName);

            assertEquals(targetUserName, productDTO.getVoteSelectedName());

            verify(sessionService).saveSession(roomId, currentUserName, productDTO);
            verify(messagingTemplate).convertAndSend("/topic/expulsion/" + roomCode, currentUserName);
        }

        @Test
        public void shouldThrowEntityNotFoundWhenRoomCodeInvalid() {
            Long roomId = 1L;
            String targetUserName = "targetUser";

            when(roomRepository.findCodeToConnectById(roomId)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> roomPlayerService.playerExpulsion(roomId, targetUserName)
            );

            assertEquals("Code is not valid or something went wrong", exception.getMessage());

            verify(authService, never()).getCurrentUserName();
            verify(sessionService, never()).getSession(anyLong(), anyString());
            verify(sessionService, never()).saveSession(anyLong(), anyString(), any());
            verify(messagingTemplate, never()).convertAndSend(anyString(), anyString());
        }

        @Test
        public void shouldThrowNullPointerExceptionWhenSessionIsNull() {
            Long roomId = 1L;
            String targetUserName = "targetUser";
            String currentUserName = "currentUser";
            String roomCode = "ROOM_CODE";

            when(roomRepository.findCodeToConnectById(roomId)).thenReturn(Optional.of(roomCode));
            when(authService.getCurrentUserName()).thenReturn(currentUserName);
            when(sessionService.getSession(roomId, currentUserName)).thenReturn(null);

            assertThrows(
                    NullPointerException.class,
                    () -> roomPlayerService.playerExpulsion(roomId, targetUserName)
            );

            verify(sessionService, never()).saveSession(anyLong(), anyString(), any());
            verify(messagingTemplate, never()).convertAndSend(anyString(), anyString());
        }
    }

    @Nested
    class NextMove {

        @Test
        public void shouldProcessNextMoveSuccessfullyAndDecrementTimers() {
            Long roomId = 1L;
            String creatorName = "creatorUser";
            String roomCode = "ROOM_CODE";

            User creator = User.builder().username(creatorName).build();
            Room room = Room.builder().id(roomId).user(creator).build();

            // Користувач, у якого таймер дійде до 0
            ProductDTO p1 = ProductDTO.builder()
                    .userName("user1")
                    .timeOfStunned(1)
                    .timeOfProtection(0)
                    .build();

            // Користувач, у якого таймер зменшиться, але не стане 0
            ProductDTO p2 = ProductDTO.builder()
                    .userName("user2")
                    .timeOfStunned(0)
                    .timeOfProtection(2)
                    .build();

            List<ProductDTO> sessions = List.of(p1, p2);

            when(authService.getCurrentUserName()).thenReturn(creatorName);
            when(roomRepository.findCodeToConnectById(roomId)).thenReturn(Optional.of(roomCode));
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
            when(sessionService.getAllSessionByRoomId(roomId)).thenReturn(sessions);

            roomPlayerService.nextMove(roomId);

            // Перевірка загального сповіщення
            verify(messagingTemplate).convertAndSend("/topic/next_move" + roomCode, roomId);

            // Перевірка віднімання
            assertEquals(0, p1.getTimeOfStunned());
            assertEquals(1, p2.getTimeOfProtection());

            // Перевірка надсилання індивідуальних повідомлень
            verify(messagingTemplate).convertAndSendToUser("user1", "/topic/stun" + roomCode, false);
            verify(messagingTemplate, never()).convertAndSendToUser(eq("user2"), eq("/topic/protect" + roomCode), anyBoolean());

            // Перевірка збереження сесій
            verify(sessionService).saveSession(roomId, "user1", p1);
            verify(sessionService).saveSession(roomId, "user2", p2);
        }

        @Test
        public void shouldThrowIllegalArgumentWhenRoomCodeIsNotFound() {
            Long roomId = 1L;
            when(authService.getCurrentUserName()).thenReturn("user");
            when(roomRepository.findCodeToConnectById(roomId)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> roomPlayerService.nextMove(roomId)
            );

            assertEquals("Room is not exist", exception.getMessage());
            verify(roomRepository, never()).findById(anyLong());
        }

        @Test
        public void shouldThrowEntityNotFoundWhenRoomIsNotFound() {
            Long roomId = 1L;
            String userName = "user";
            when(authService.getCurrentUserName()).thenReturn(userName);
            when(roomRepository.findCodeToConnectById(roomId)).thenReturn(Optional.of("CODE"));
            when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> roomPlayerService.nextMove(roomId)
            );

            assertEquals("Room is not exist in function nextMove", exception.getMessage());
        }

        @Test
        public void shouldThrowIllegalArgumentWhenUserIsNotCreator() {
            Long roomId = 1L;
            String userName = "currentUser";

            User creator = User.builder().username("differentCreator").build();
            Room room = Room.builder().id(roomId).user(creator).build();

            when(authService.getCurrentUserName()).thenReturn(userName);
            when(roomRepository.findCodeToConnectById(roomId)).thenReturn(Optional.of("CODE"));
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> roomPlayerService.nextMove(roomId)
            );

            assertEquals("User is not a creator", exception.getMessage());
            verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
        }

        @Test
        public void shouldThrowIllegalArgumentWhenSessionsListIsEmpty() {
            Long roomId = 1L;
            String creatorName = "creatorUser";

            User creator = User.builder().username(creatorName).build();
            Room room = Room.builder().id(roomId).user(creator).build();

            when(authService.getCurrentUserName()).thenReturn(creatorName);
            when(roomRepository.findCodeToConnectById(roomId)).thenReturn(Optional.of("CODE"));
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
            when(sessionService.getAllSessionByRoomId(roomId)).thenReturn(Collections.emptyList());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> roomPlayerService.nextMove(roomId)
            );

            assertEquals("Room by id " + roomId + "is not exist", exception.getMessage());
        }
    }

    @Nested
    class VotingResults {

        @Test
        public void shouldCalculateAndSendResultsWhenAllVoted() {
            Long roomId = 1L;
            String creatorName = "creatorUser";
            String roomCode = "ROOM_CODE";

            User creator = User.builder().username(creatorName).build();
            Room room = Room.builder().id(roomId).user(creator).build();

            ProductDTO currentUserDto = ProductDTO.builder().userName(creatorName).build();

            // Три гравці: два проголосували за "targetUser1", один за "targetUser2"
            ProductDTO p1 = ProductDTO.builder().voteSelectedName("targetUser1").build();
            ProductDTO p2 = ProductDTO.builder().voteSelectedName("targetUser1").build();
            ProductDTO p3 = ProductDTO.builder().voteSelectedName("targetUser2").build();
            List<ProductDTO> sessions = List.of(p1, p2, p3);

            when(authService.getCurrentUserName()).thenReturn(creatorName);
            when(sessionService.getSession(roomId, creatorName)).thenReturn(currentUserDto);
            when(roomRepository.findRoomAndUserBy(roomId)).thenReturn(Optional.of(room));
            when(roomRepository.findCodeToConnectById(roomId)).thenReturn(Optional.of(roomCode));
            when(sessionService.getAllSessionByRoomId(roomId)).thenReturn(sessions);

            roomPlayerService.votingResults(roomId);

            ArgumentCaptor<List<String>> keysCaptor = ArgumentCaptor.forClass(List.class);
            verify(messagingTemplate).convertAndSend(eq("/topic/voting_results/" + roomCode), keysCaptor.capture());

            List<String> resultKeys = keysCaptor.getValue();
            assertEquals(1, resultKeys.size());
            assertTrue(resultKeys.contains("targetUser1"));
        }

        @Test
        public void shouldThrowIllegalArgumentWhenUserIsNotCreator() {
            Long roomId = 1L;
            String currentUserName = "someUser";

            User creator = User.builder().username("creatorUser").build();
            Room room = Room.builder().id(roomId).user(creator).build();

            ProductDTO currentUserDto = ProductDTO.builder().userName(currentUserName).build();

            when(authService.getCurrentUserName()).thenReturn(currentUserName);
            when(sessionService.getSession(roomId, currentUserName)).thenReturn(currentUserDto);
            when(roomRepository.findRoomAndUserBy(roomId)).thenReturn(Optional.of(room));

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> roomPlayerService.votingResults(roomId)
            );

            assertEquals("User is not a creator", exception.getMessage());
            verify(roomRepository, never()).findCodeToConnectById(anyLong());
            verify(sessionService, never()).getAllSessionByRoomId(anyLong());
        }

        @Test
        public void shouldThrowNoResultExceptionWhenNotAllVoted() {
            Long roomId = 1L;
            String creatorName = "creatorUser";
            String roomCode = "ROOM_CODE";

            User creator = User.builder().username(creatorName).build();
            Room room = Room.builder().id(roomId).user(creator).build();

            ProductDTO currentUserDto = ProductDTO.builder().userName(creatorName).build();

            // Один гравець не проголосував (voteSelectedName == null)
            ProductDTO p1 = ProductDTO.builder().voteSelectedName("targetUser1").build();
            ProductDTO p2 = ProductDTO.builder().voteSelectedName(null).build();
            List<ProductDTO> sessions = List.of(p1, p2);

            when(authService.getCurrentUserName()).thenReturn(creatorName);
            when(sessionService.getSession(roomId, creatorName)).thenReturn(currentUserDto);
            when(roomRepository.findRoomAndUserBy(roomId)).thenReturn(Optional.of(room));
            when(roomRepository.findCodeToConnectById(roomId)).thenReturn(Optional.of(roomCode));
            when(sessionService.getAllSessionByRoomId(roomId)).thenReturn(sessions);

            NoResultException exception = assertThrows(
                    NoResultException.class,
                    () -> roomPlayerService.votingResults(roomId)
            );

            assertEquals("Not all participants voted", exception.getMessage());
            verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
        }

        @Test
        public void shouldThrowIllegalArgumentWhenSessionsListIsEmpty() {
            Long roomId = 1L;
            String creatorName = "creatorUser";
            String roomCode = "ROOM_CODE";

            User creator = User.builder().username(creatorName).build();
            Room room = Room.builder().id(roomId).user(creator).build();

            ProductDTO currentUserDto = ProductDTO.builder().userName(creatorName).build();

            when(authService.getCurrentUserName()).thenReturn(creatorName);
            when(sessionService.getSession(roomId, creatorName)).thenReturn(currentUserDto);
            when(roomRepository.findRoomAndUserBy(roomId)).thenReturn(Optional.of(room));
            when(roomRepository.findCodeToConnectById(roomId)).thenReturn(Optional.of(roomCode));
            when(sessionService.getAllSessionByRoomId(roomId)).thenReturn(List.of());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> roomPlayerService.votingResults(roomId)
            );

            assertEquals("Room by id " + roomId + "is not exist", exception.getMessage());
        }
    }
    private PlayerProjection getTestPlayerProjection(String name, Long playerId) {
        return new PlayerProjection() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public Long getPlayerId() {
                return playerId;
            }
        };
    }
}
