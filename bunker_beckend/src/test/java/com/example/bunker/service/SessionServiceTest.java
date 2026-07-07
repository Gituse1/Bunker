package com.example.bunker.service;

import com.example.bunker.dto.ProductDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private RedisTemplate<String, ProductDTO> redisTemplate;

    @Mock
    private ValueOperations<String, ProductDTO> valueOperations;

    @InjectMocks
    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        // Налаштування моку для opsForValue, оскільки він використовується майже в усіх методах
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    class UpdateSession {

        @Test
        public void shouldUpdateExistingSession() {
            Long roomId = 1L;
            String userName = "testUser";
            String expectedKey = "session1:testUser";
            ProductDTO existingDto = ProductDTO.builder().userName("oldName").build();

            when(valueOperations.get(expectedKey)).thenReturn(existingDto);

            Consumer<ProductDTO> updateLogic = dto -> dto.setUserName("newName");

            sessionService.updateSession(roomId, userName, updateLogic);

            assertEquals("newName", existingDto.getUserName());
            verify(valueOperations).set(expectedKey, existingDto);
        }

        @Test
        public void shouldCreateNewSessionIfNotFound() {
            Long roomId = 1L;
            String userName = "testUser";
            String expectedKey = "session1:testUser";

            when(valueOperations.get(expectedKey)).thenReturn(null);

            Consumer<ProductDTO> updateLogic = dto -> dto.setUserName("newName");

            sessionService.updateSession(roomId, userName, updateLogic);

            ArgumentCaptor<ProductDTO> dtoCaptor = ArgumentCaptor.forClass(ProductDTO.class);
            verify(valueOperations).set(eq(expectedKey), dtoCaptor.capture());

            ProductDTO savedDto = dtoCaptor.getValue();
            assertNotNull(savedDto);
            assertEquals("newName", savedDto.getUserName());
        }
    }

    @Nested
    class SaveSession {

        @Test
        public void shouldSaveSession() {
            Long roomId = 1L;
            String userName = "testUser";
            String expectedKey = "session1:testUser";
            ProductDTO dtoToSave = ProductDTO.builder().userName(userName).build();

            sessionService.saveSession(roomId, userName, dtoToSave);

            verify(valueOperations).set(expectedKey, dtoToSave);
        }
    }

    @Nested
    class GetSession {

        @Test
        public void shouldReturnSession() {
            Long roomId = 1L;
            String userName = "testUser";
            String expectedKey = "session1:testUser";
            ProductDTO expectedDto = ProductDTO.builder().userName(userName).build();

            when(valueOperations.get(expectedKey)).thenReturn(expectedDto);

            ProductDTO result = sessionService.getSession(roomId, userName);

            assertNotNull(result);
            assertEquals(expectedDto, result);
        }
    }

    @Nested
    class GetAllSessionByRoomId {

        @Test
        public void shouldReturnFilteredSessions() {
            Long roomId = 1L;
            String keyPattern = "session1:*";
            Set<String> keys = new HashSet<>(Arrays.asList("session1:user1", "session1:user2"));

            ProductDTO dto1 = ProductDTO.builder().userName("user1").build();
            ProductDTO dto2 = ProductDTO.builder().userName("user2").build();
            // Імітуємо повернення списку, де є один null елемент
            List<ProductDTO> multiGetResult = Arrays.asList(dto1, null, dto2);

            when(redisTemplate.keys(keyPattern)).thenReturn(keys);
            when(valueOperations.multiGet(keys)).thenReturn(multiGetResult);

            List<ProductDTO> result = sessionService.getAllSessionByRoomId(roomId);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.contains(dto1));
            assertTrue(result.contains(dto2));
        }

        @Test
        public void shouldThrowExceptionWhenKeysNull() {
            Long roomId = 1L;
            String keyPattern = "session1:*";

            when(redisTemplate.keys(keyPattern)).thenReturn(null);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> sessionService.getAllSessionByRoomId(roomId)
            );

            // Текст має точно збігатися з тим, що ти передаєш у throw new IllegalArgumentException(...)
            assertEquals("Keys is null", exception.getMessage());
            verify(valueOperations, never()).multiGet(anySet());
        }

        @Test
        public void shouldReturnEmptyListWhenKeysEmpty() {
            Long roomId = 1L;
            String keyPattern = "session1:*";

            when(redisTemplate.keys(keyPattern)).thenReturn(Collections.emptySet());

            List<ProductDTO> result = sessionService.getAllSessionByRoomId(roomId);

            assertTrue(result.isEmpty());
            verify(valueOperations, never()).multiGet(anySet());
        }

        @Test
        public void shouldReturnEmptyListWhenMultiGetReturnsNull() {
            Long roomId = 1L;
            String keyPattern = "session1:*";
            Set<String> keys = Collections.singleton("session1:user1");

            when(redisTemplate.keys(keyPattern)).thenReturn(keys);
            when(valueOperations.multiGet(keys)).thenReturn(null);

            List<ProductDTO> result = sessionService.getAllSessionByRoomId(roomId);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class DeleteSession {

        @Test
        public void shouldDeleteSession() {
            Long roomId = 1L;
            String userName = "testUser";
            String expectedKey = "session1:testUser";

            sessionService.deleteSession(roomId, userName);

            verify(redisTemplate).delete(expectedKey);
        }
    }
}