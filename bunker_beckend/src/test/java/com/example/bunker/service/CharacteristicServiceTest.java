package com.example.bunker.service;

import com.example.bunker.dto.ProductDTO;
import com.example.bunker.model.CharacteristicPlayer;
import com.example.bunker.repository.CharacteristicRepository;
import com.example.bunker.repository.PlayerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CharacteristicServiceTest {

    @Mock
    private CharacteristicRepository characteristicRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private SessionService sessionService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private CharacteristicService characteristicService;

    @Nested
    class CreateCharacteristic {

        @Test
        @SuppressWarnings("unchecked")
        public void shouldCreateCharacteristicAndUpdateSessionSuccessfully() {
            Long roomId = 1L;
            String username = "testUser";
            Long playerId = 100L;
            Long savedCharId = 50L;

            ProductDTO sessionDto = ProductDTO.builder().playerId(playerId).build();
            CharacteristicPlayer savedPlayer = CharacteristicPlayer.builder().id(savedCharId).build();

            when(authService.getCurrentUserName()).thenReturn(username);
            when(characteristicRepository.save(any(CharacteristicPlayer.class))).thenReturn(savedPlayer);
            when(sessionService.getSession(roomId, username)).thenReturn(sessionDto);
            when(playerRepository.updateCharacter(playerId, savedPlayer)).thenReturn(1);

            CharacteristicPlayer result = characteristicService.createCharacteristic(roomId);

            assertNotNull(result);
            assertEquals(savedCharId, result.getId());

            // Перевірка, що об'єкт перед збереженням має заповнені поля
            ArgumentCaptor<CharacteristicPlayer> charCaptor = ArgumentCaptor.forClass(CharacteristicPlayer.class);
            verify(characteristicRepository).save(charCaptor.capture());
            CharacteristicPlayer capturedChar = charCaptor.getValue();

            assertNotNull(capturedChar.getStateOfHealth());
            assertNotNull(capturedChar.getGrown());
            assertNotNull(capturedChar.getFigure());
            assertNotNull(capturedChar.getPsyhologicalState());
            assertNotNull(capturedChar.getSecret());

            verify(playerRepository).updateCharacter(playerId, savedPlayer);

            // Перевірка виконання лямбди для оновлення сесії
            ArgumentCaptor<Consumer<ProductDTO>> consumerCaptor = ArgumentCaptor.forClass(Consumer.class);
            verify(sessionService).updateSession(eq(roomId), eq(username), consumerCaptor.capture());

            ProductDTO testDtoToUpdate = new ProductDTO();
            consumerCaptor.getValue().accept(testDtoToUpdate);
            assertEquals(savedCharId, testDtoToUpdate.getCharacterId());
        }

        @Test
        public void shouldThrowEntityNotFoundWhenPlayerUpdateFails() {
            Long roomId = 1L;
            String username = "testUser";
            Long playerId = 100L;

            ProductDTO sessionDto = ProductDTO.builder().playerId(playerId).build();
            CharacteristicPlayer savedPlayer = CharacteristicPlayer.builder().id(50L).build();

            when(authService.getCurrentUserName()).thenReturn(username);
            when(characteristicRepository.save(any(CharacteristicPlayer.class))).thenReturn(savedPlayer);
            when(sessionService.getSession(roomId, username)).thenReturn(sessionDto);
            when(playerRepository.updateCharacter(playerId, savedPlayer)).thenReturn(0);

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> characteristicService.createCharacteristic(roomId)
            );

            assertEquals("updating player failed when we connect characteristic data" + username, exception.getMessage());

            // Сесія не повинна оновлюватися, якщо виникла помилка в БД
            verify(sessionService, never()).updateSession(anyLong(), anyString(), any());
        }
    }
}