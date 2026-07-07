package com.example.bunker.service;

import com.example.bunker.dto.Room.AllRoomsResponse;
import com.example.bunker.dto.Room.RoomResponse;
import com.example.bunker.model.Room;
import com.example.bunker.model.RoomPlayer;
import com.example.bunker.model.User;
import com.example.bunker.repository.RoomRepository;
import com.example.bunker.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private AuthService authService;

    @Mock
    private RoomPlayerService roomPlayerService;

    @InjectMocks
    @Spy
    private RoomService roomService;

    @Nested
    class CreateRoom {

        @Test
        public void shouldCreateRoomSuccessfully() {
            String userName = "testUser";
            String generatedCode = "ABCDEFGH";

            User user = User.builder().username(userName).build();
            Room savedRoom = Room.builder().id(10L).codeToConnect(generatedCode).build();
            RoomPlayer roomPlayer = RoomPlayer.builder().id(20L).build();

            when(authService.getCurrentUserName()).thenReturn(userName);
            when(userRepository.findByUsername(userName)).thenReturn(Optional.of(user));
            doReturn(generatedCode).when(roomService).generateToken();
            when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);
            when(roomPlayerService.createRoomPlayer(savedRoom)).thenReturn(roomPlayer);

            RoomResponse response = roomService.createRoom();

            assertNotNull(response);
            assertEquals(20L, response.getId());
            assertEquals(generatedCode, response.getCodeToConnect());

            ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
            verify(roomRepository).save(roomCaptor.capture());
            Room capturedRoom = roomCaptor.getValue();

            assertEquals(user, capturedRoom.getUser());
            assertFalse(capturedRoom.isIfFinished());
            assertNotNull(capturedRoom.getCreatedAt());
            assertEquals(generatedCode, capturedRoom.getCodeToConnect());
        }

        @Test
        public void shouldThrowRuntimeExceptionWhenUserNotFound() {
            String userName = "testUser";

            when(authService.getCurrentUserName()).thenReturn(userName);
            when(userRepository.findByUsername(userName)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> roomService.createRoom()
            );

            assertEquals("Data authorisation was damaged", exception.getMessage());
            verify(roomRepository, never()).save(any(Room.class));
            verify(roomPlayerService, never()).createRoomPlayer(any());
        }
    }

    @Nested
    class GetAllRooms {

        @Test
        public void shouldReturnListOfRooms() {
            String userName = "testUser";

            Room room1 = Room.builder()
                    .id(1L)
                    .createdAt(LocalDateTime.now())
                    .ifFinished(true)
                    .codeToConnect("CODE0001")
                    .build();

            Room room2 = Room.builder()
                    .id(2L)
                    .createdAt(LocalDateTime.now())
                    .ifFinished(false)
                    .codeToConnect("CODE0002")
                    .build();

            when(authService.getCurrentUserName()).thenReturn(userName);
            when(roomRepository.roomsByUserName(userName)).thenReturn(List.of(room1, room2));

            List<AllRoomsResponse> responses = roomService.getAllRooms();

            assertEquals(2, responses.size());

            assertEquals(1L, responses.get(0).getId());
            assertTrue(responses.get(0).isFinished());
            assertEquals("CODE0001", responses.get(0).getCodeToConnect());

            assertEquals(2L, responses.get(1).getId());
            assertFalse(responses.get(1).isFinished());
            assertEquals("CODE0002", responses.get(1).getCodeToConnect());
        }

        @Test
        public void shouldThrowIllegalArgumentExceptionWhenListIsEmpty() {
            String userName = "testUser";

            when(authService.getCurrentUserName()).thenReturn(userName);
            when(roomRepository.roomsByUserName(userName)).thenReturn(Collections.emptyList());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> roomService.getAllRooms()
            );

            assertEquals("The user has not created any rooms yet.", exception.getMessage());
        }
    }

    @Nested
    class GenerateToken {

        @Test
        public void shouldGenerateTokenOnFirstAttempt() {
            when(roomRepository.findRoomByCodeToConnect(anyString())).thenReturn(Optional.empty());

            String token = roomService.generateToken();

            assertNotNull(token);
            assertEquals(8, token.length());
            verify(roomRepository, times(1)).findRoomByCodeToConnect(token);
        }

        @Test
        public void shouldRegenerateTokenIfCollisionOccurs() {
            Room existingRoom = Room.builder().build();

            // Імітація: перший згенерований код вже існує, другий - унікальний
            when(roomRepository.findRoomByCodeToConnect(anyString()))
                    .thenReturn(Optional.of(existingRoom))
                    .thenReturn(Optional.empty());

            String token = roomService.generateToken();

            assertNotNull(token);
            assertEquals(8, token.length());
            verify(roomRepository, times(2)).findRoomByCodeToConnect(anyString());
        }
    }
}