package com.example.bunker.service;

import com.example.bunker.dto.Characteristic.CharacteristicArtifactStealing;
import com.example.bunker.dto.Characteristic.CharacteristicShowCharacteristicRequest;
import com.example.bunker.dto.Characteristic.CharacteristicSourceDto;
import com.example.bunker.dto.Player.GameEventDto;
import com.example.bunker.dto.Player.PlayerEffectUpdateDto;
import com.example.bunker.dto.ProductDTO;
import com.example.bunker.dto.ProductDTORequest;
import com.example.bunker.model.*;
import com.example.bunker.model.characteristic.Characteristic;
import com.example.bunker.model.characteristic.Figure;
import com.example.bunker.model.characteristic.PhysicalCondition;
import com.example.bunker.model.characteristic.PsychologicalState;
import com.example.bunker.projection.CharacteristicSource;
import com.example.bunker.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtifactServiceTest {

    @Mock
    private AuthService authService;
    @Mock
    private SessionService sessionService;
    @Mock
    private CharacteristicRepository characteristicRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private VisibilityOfCharacteristicRepository visibilityOfCharacteristicRepository;
    @Mock
    private ArtifactHeroCatalogRepository artifactHeroCatalogRepository;
    @Mock
    private ArtifactRandomCatalogRepository artifactRandomCatalogRepository;
    @Mock
    private HeroRepository heroRepository;
    @Mock
    private EffectRepository effectRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    @Spy
    private ArtifactService artifactService;

    @Nested
    class UsePurification {

        @Test
        public void shouldUpdatePhysicalConditionWhenArtifactIdMatches() {
            Long artifactId = 100L;
            Long roomId = 1L;
            Long targetPlayerId = 2L;

            ProductDTORequest mockRequest = getTestProductDTORequest(artifactId, 200L);
            CharacteristicPlayer characteristicPlayer = getTestCharacteristicPlayer(PhysicalCondition.WEAK);

            doReturn(mockRequest).when(artifactService).auditData(roomId, targetPlayerId);
            when(characteristicRepository.findById(mockRequest.getProductDTO2().getCharacterId()))
                    .thenReturn(Optional.of(characteristicPlayer));
            when(playerRepository.findUserNameByPlayerId(targetPlayerId)).thenReturn(Optional.of("targetUser"));
            when(authService.getCurrentUserName()).thenReturn("currentUser");
            when(roomRepository.findCodeToConnectById(roomId)).thenReturn(Optional.of("ROOM_CODE"));

            artifactService.usePurification(artifactId, roomId, targetPlayerId, Characteristic.PHYSICAL_CONDITION);

            verify(characteristicRepository).save(characteristicPlayer);
            assertEquals(PhysicalCondition.WOUNDED, characteristicPlayer.getPhysicalCondition());

            ArgumentCaptor<GameEventDto> eventCaptor = ArgumentCaptor.forClass(GameEventDto.class);
            verify(messagingTemplate).convertAndSend(eq("/topic/purification.ROOM_CODE"), eventCaptor.capture());

            GameEventDto sentEvent = eventCaptor.getValue();
            assertEquals("targetUser", sentEvent.playerName());
            assertEquals(ActionTypeArtifact.PURIFICATION, sentEvent.changeType());
            assertEquals(Characteristic.PHYSICAL_CONDITION, sentEvent.characteristic());
            assertEquals(PhysicalCondition.WOUNDED.toString(), sentEvent.updatedCharacteristic());
        }

        @Test
        public void shouldThrowNoResultExceptionWhenPhysicalConditionIsStrong() {
            Long artifactId = 100L;
            Long roomId = 1L;
            Long targetPlayerId = 2L;

            ProductDTORequest mockRequest = getTestProductDTORequest(artifactId, 200L);
            CharacteristicPlayer characteristicPlayer = getTestCharacteristicPlayer(PhysicalCondition.STRONG);

            doReturn(mockRequest).when(artifactService).auditData(roomId, targetPlayerId);
            when(characteristicRepository.findById(mockRequest.getProductDTO2().getCharacterId()))
                    .thenReturn(Optional.of(characteristicPlayer));

            NoResultException exception = assertThrows(
                    NoResultException.class,
                    () -> artifactService.usePurification(artifactId, roomId, targetPlayerId, Characteristic.PHYSICAL_CONDITION)
            );

            assertEquals("There is nothing stronger than strong", exception.getMessage());
            verify(characteristicRepository, never()).save(any());
            verify(messagingTemplate, never()).convertAndSend(anyString(), any(GameEventDto.class));
        }

        @Test
        public void shouldThrowNullPointerExceptionWhenPhysicalConditionIsInvalid() {
            Long artifactId = 100L;
            Long roomId = 1L;
            Long targetPlayerId = 2L;

            ProductDTORequest mockRequest = getTestProductDTORequest(artifactId, 200L);
            CharacteristicPlayer characteristicPlayer = getTestCharacteristicPlayer(null);

            doReturn(mockRequest).when(artifactService).auditData(roomId, targetPlayerId);
            when(characteristicRepository.findById(mockRequest.getProductDTO2().getCharacterId()))
                    .thenReturn(Optional.of(characteristicPlayer));

            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> artifactService.usePurification(artifactId, roomId, targetPlayerId, Characteristic.PHYSICAL_CONDITION)
            );
            assertNotNull(exception.getMessage());
        }

        @Test
        public void shouldThrowEntityNotFoundWhenCharacteristicIdNotFound() {
            Long artifactId = 100L;
            Long roomId = 1L;
            Long targetPlayerId = 2L;

            ProductDTORequest mockRequest = getTestProductDTORequest(artifactId, 200L);

            doReturn(mockRequest).when(artifactService).auditData(roomId, targetPlayerId);
            when(characteristicRepository.findById(mockRequest.getProductDTO2().getCharacterId()))
                    .thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> artifactService.usePurification(artifactId, roomId, targetPlayerId, Characteristic.PHYSICAL_CONDITION)
            );

            assertEquals("In Redis characteristicId is not correct", exception.getMessage());
            verify(characteristicRepository, never()).save(any());
        }

        @Test
        public void shouldDoNothingWhenArtifactIdDoesNotMatchFirstUser() {
            Long artifactId = 999L;
            Long roomId = 1L;
            Long targetPlayerId = 2L;

            ProductDTORequest mockRequest = getTestProductDTORequest(100L, 200L);

            doReturn(mockRequest).when(artifactService).auditData(roomId, targetPlayerId);

            artifactService.usePurification(artifactId, roomId, targetPlayerId, Characteristic.PHYSICAL_CONDITION);

            verify(characteristicRepository, never()).findById(anyLong());
            verify(characteristicRepository, never()).save(any());
            verify(messagingTemplate, never()).convertAndSend(anyString(), any(GameEventDto.class));
        }
    }

    @Nested
    class UnderEffect {

        @Test
        public void shouldApplyEffectWhenArtifactMatchesRand1() {
            Long roomId = 1L;
            Long artifactId = 100L;
            String userName = "user1";

            ProductDTO mockUser = ProductDTO.builder()
                    .artifactRand1Id(artifactId)
                    .artifactRand2Id(200L)
                    .effectId(10L)
                    .build();

            Effect mockEffect = Effect.builder().id(10L).build();
            Effects effectsMock = mock(Effects.class);

            when(authService.getCurrentUserName()).thenReturn(userName);
            when(sessionService.getSession(roomId, userName)).thenReturn(mockUser);
            when(effectsMock.isUnderEffect(mockUser)).thenReturn(false);
            when(effectRepository.findById(10L)).thenReturn(Optional.of(mockEffect));
            when(effectsMock.setEffectToDto(mockUser)).thenReturn(mockUser);
            when(effectsMock.setEffectToEffects(mockEffect)).thenReturn(mockEffect);
            when(effectsMock.name()).thenReturn("MOCK_EFFECT");

            PlayerEffectUpdateDto result = artifactService.underEffect(roomId, artifactId, effectsMock);

            assertNotNull(result);
            verify(effectRepository).save(mockEffect);
            verify(sessionService).saveSession(roomId, userName, mockUser);
        }

        @Test
        public void shouldThrowIllegalArgumentExceptionWhenUserIsNull() {
            Long roomId = 1L;
            Long artifactId = 100L;
            String userName = "user1";
            Effects effectsMock = mock(Effects.class);

            when(authService.getCurrentUserName()).thenReturn(userName);
            when(sessionService.getSession(roomId, userName)).thenReturn(null);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> artifactService.underEffect(roomId, artifactId, effectsMock)
            );

            assertEquals("Data in Redis was broken or room Id is not correct", exception.getMessage());
            verify(effectRepository, never()).save(any());
            verify(sessionService, never()).saveSession(anyLong(), anyString(), any());
        }

        @Test
        public void shouldThrowNoResultExceptionWhenAlreadyUnderEffect() {
            Long roomId = 1L;
            Long artifactId = 100L;
            String userName = "user1";

            ProductDTO mockUser = ProductDTO.builder()
                    .artifactRand1Id(artifactId)
                    .artifactRand2Id(200L)
                    .build();

            Effects effectsMock = mock(Effects.class);

            when(authService.getCurrentUserName()).thenReturn(userName);
            when(sessionService.getSession(roomId, userName)).thenReturn(mockUser);
            when(effectsMock.isUnderEffect(mockUser)).thenReturn(true);

            NoResultException exception = assertThrows(
                    NoResultException.class,
                    () -> artifactService.underEffect(roomId, artifactId, effectsMock)
            );

            assertEquals("There is nothing under the effect to under", exception.getMessage());
            verify(effectRepository, never()).save(any());
            verify(sessionService, never()).saveSession(anyLong(), anyString(), any());
        }

        @Test
        public void shouldThrowEntityNotFoundExceptionWhenEffectNotFoundInDb() {
            Long roomId = 1L;
            Long artifactId = 100L;
            String userName = "user1";

            ProductDTO mockUser = ProductDTO.builder()
                    .artifactRand1Id(artifactId)
                    .artifactRand2Id(200L)
                    .effectId(10L)
                    .build();

            Effects effectsMock = mock(Effects.class);

            when(authService.getCurrentUserName()).thenReturn(userName);
            when(sessionService.getSession(roomId, userName)).thenReturn(mockUser);
            when(effectsMock.isUnderEffect(mockUser)).thenReturn(false);
            when(effectRepository.findById(10L)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> artifactService.underEffect(roomId, artifactId, effectsMock)
            );

            assertEquals("Effect not found in function underEffect", exception.getMessage());
            verify(effectRepository, never()).save(any());
            verify(sessionService, never()).saveSession(anyLong(), anyString(), any());
        }

        @Test
        public void shouldReturnDtoWithoutSavingWhenArtifactIdDoesNotMatch() {
            Long roomId = 1L;
            Long artifactId = 999L;
            String userName = "user1";

            ProductDTO mockUser = ProductDTO.builder()
                    .artifactRand1Id(100L)
                    .artifactRand2Id(200L)
                    .build();

            Effects effectsMock = mock(Effects.class);
            when(authService.getCurrentUserName()).thenReturn(userName);
            when(sessionService.getSession(roomId, userName)).thenReturn(mockUser);
            when(effectsMock.name()).thenReturn("MOCK_EFFECT");

            PlayerEffectUpdateDto result = artifactService.underEffect(roomId, artifactId, effectsMock);

            assertNotNull(result);
            verify(effectRepository, never()).findById(anyLong());
            verify(effectRepository, never()).save(any());
            verify(sessionService, never()).saveSession(anyLong(), anyString(), any());
        }
    }

    @Nested
    class UseCurse {

        @Test
        public void shouldDowngradePhysicalConditionWhenArtifactIdMatches() {
            Long artifactId = 100L;
            Long roomId = 1L;
            Long targetPlayerId = 2L;

            ProductDTORequest mockRequest = getTestProductDTORequest(artifactId, 200L);
            CharacteristicPlayer characteristicPlayer = getTestCharacteristicPlayer(PhysicalCondition.STRONG);

            doReturn(mockRequest).when(artifactService).auditData(roomId, targetPlayerId);
            when(characteristicRepository.findById(mockRequest.getProductDTO2().getCharacterId()))
                    .thenReturn(Optional.of(characteristicPlayer));
            when(characteristicRepository.save(any(CharacteristicPlayer.class))).thenAnswer(i -> i.getArgument(0));
            when(playerRepository.findUserNameByPlayerId(targetPlayerId)).thenReturn(Optional.of("targetUser"));
            when(authService.getCurrentUserName()).thenReturn("currentUser");
            when(roomRepository.findCodeToConnectById(roomId)).thenReturn(Optional.of("ROOM_CODE"));

            artifactService.useCurse(artifactId, roomId, targetPlayerId, Characteristic.PHYSICAL_CONDITION);

            verify(characteristicRepository).save(characteristicPlayer);
            assertEquals(PhysicalCondition.DISABILITY, characteristicPlayer.getPhysicalCondition());

            ArgumentCaptor<GameEventDto> eventCaptor = ArgumentCaptor.forClass(GameEventDto.class);
            verify(messagingTemplate).convertAndSend(eq("/topic/room.ROOM_CODE"), eventCaptor.capture());

            GameEventDto sentEvent = eventCaptor.getValue();
            assertEquals("targetUser", sentEvent.playerName());
            assertEquals(ActionTypeArtifact.CURSE, sentEvent.changeType());
            assertEquals(Characteristic.PHYSICAL_CONDITION, sentEvent.characteristic());
            assertEquals(PhysicalCondition.DISABILITY.toString(), sentEvent.updatedCharacteristic());
        }

        @Test
        public void shouldThrowNoResultExceptionWhenPhysicalConditionIsWeak() {
            Long artifactId = 100L;
            Long roomId = 1L;
            Long targetPlayerId = 2L;

            ProductDTORequest mockRequest = getTestProductDTORequest(artifactId, 200L);
            CharacteristicPlayer characteristicPlayer = getTestCharacteristicPlayer(PhysicalCondition.WEAK);

            doReturn(mockRequest).when(artifactService).auditData(roomId, targetPlayerId);
            when(characteristicRepository.findById(mockRequest.getProductDTO2().getCharacterId()))
                    .thenReturn(Optional.of(characteristicPlayer));

            NoResultException exception = assertThrows(
                    NoResultException.class,
                    () -> artifactService.useCurse(artifactId, roomId, targetPlayerId, Characteristic.PHYSICAL_CONDITION)
            );

            assertEquals("There is nothing weaker than weak", exception.getMessage());
            verify(characteristicRepository, never()).save(any());
            verify(messagingTemplate, never()).convertAndSend(anyString(), any(GameEventDto.class));
        }

        @Test
        public void shouldThrowNullPointerExceptionWhenConditionIsNull() {
            Long artifactId = 100L;
            Long roomId = 1L;
            Long targetPlayerId = 2L;

            ProductDTORequest mockRequest = getTestProductDTORequest(artifactId, 200L);
            CharacteristicPlayer characteristicPlayer = getTestCharacteristicPlayer(null);

            doReturn(mockRequest).when(artifactService).auditData(roomId, targetPlayerId);
            when(characteristicRepository.findById(mockRequest.getProductDTO2().getCharacterId()))
                    .thenReturn(Optional.of(characteristicPlayer));

            assertThrows(
                    NullPointerException.class,
                    () -> artifactService.useCurse(artifactId, roomId, targetPlayerId, Characteristic.PHYSICAL_CONDITION)
            );
        }

        @Test
        public void shouldDoNothingWhenArtifactIdDoesNotMatch() {
            Long artifactId = 999L;
            Long roomId = 1L;
            Long targetPlayerId = 2L;

            ProductDTORequest mockRequest = getTestProductDTORequest(100L, 200L);

            doReturn(mockRequest).when(artifactService).auditData(roomId, targetPlayerId);

            artifactService.useCurse(artifactId, roomId, targetPlayerId, Characteristic.PHYSICAL_CONDITION);

            verify(characteristicRepository, never()).findById(anyLong());
            verify(characteristicRepository, never()).save(any());
            verify(messagingTemplate, never()).convertAndSend(anyString(), any(GameEventDto.class));
        }
    }

    @Nested
    class UseEspionage {

        @Test
        public void shouldReturnRequestWhenEspionageSuccessful() {
            Long artifactId = 100L;
            Long roomId = 1L;
            Long targetPlayerId = 2L;
            String firstUserName = "firstUser";
            String targetUserName = "targetUser";

            ProductDTO firstUser = ProductDTO.builder()
                    .artifactRand1Id(artifactId)
                    .artifactRand2Id(200L)
                    .build();
            ProductDTO targetUser = ProductDTO.builder()
                    .visibilityId(300L)
                    .build();

            // Стандартний мок повертає false для всіх boolean методів
            VisibilityOfCharacteristic visibility = mock(VisibilityOfCharacteristic.class);

            CharacteristicSourceDto sourceDto = getTestCharacteristicSourceDto();

            when(playerRepository.findUserNameByPlayerId(targetPlayerId)).thenReturn(Optional.of(targetUserName));
            when(authService.getCurrentUserName()).thenReturn(firstUserName);
            when(sessionService.getSession(roomId, firstUserName)).thenReturn(firstUser);
            when(sessionService.getSession(roomId, targetUserName)).thenReturn(targetUser);
            when(visibilityOfCharacteristicRepository.findById(300L)).thenReturn(Optional.of(visibility));

            // ВАЖЛИВЕ ВИПРАВЛЕННЯ: Для @Spy використовується виключно doReturn()
            doReturn(sourceDto).when(artifactService).getCurrentTable(targetUser, firstUser, Characteristic.PHYSICAL_CONDITION);

            CharacteristicShowCharacteristicRequest result = artifactService.useEspionage(
                    artifactId, roomId, targetPlayerId, Characteristic.PHYSICAL_CONDITION
            );

            assertNotNull(result);
            assertEquals(Characteristic.PHYSICAL_CONDITION.name(), result.nameCharacteristic());
        }

        @Test
        public void shouldThrowIllegalArgumentWhenCharacteristicIsAlreadyVisible() {
            Long artifactId = 100L;
            Long roomId = 1L;
            Long targetPlayerId = 2L;
            String firstUserName = "firstUser";
            String targetUserName = "targetUser";

            ProductDTO firstUser = ProductDTO.builder()
                    .artifactRand1Id(artifactId)
                    .artifactRand2Id(200L)
                    .build();
            ProductDTO targetUser = ProductDTO.builder()
                    .visibilityId(300L)
                    .build();

            // Примусове повернення true для будь-якого boolean геттера, який викличе enum
            VisibilityOfCharacteristic visibility = mock(VisibilityOfCharacteristic.class, invocation -> {
                if (invocation.getMethod().getReturnType().equals(boolean.class) ||
                        invocation.getMethod().getReturnType().equals(Boolean.class)) {
                    return true;
                }
                return Mockito.RETURNS_DEFAULTS.answer(invocation);
            });

            CharacteristicSourceDto sourceDto = mock(CharacteristicSourceDto.class);

            when(playerRepository.findUserNameByPlayerId(targetPlayerId)).thenReturn(Optional.of(targetUserName));
            when(authService.getCurrentUserName()).thenReturn(firstUserName);
            when(sessionService.getSession(roomId, firstUserName)).thenReturn(firstUser);
            when(sessionService.getSession(roomId, targetUserName)).thenReturn(targetUser);
            when(visibilityOfCharacteristicRepository.findById(300L)).thenReturn(Optional.of(visibility));

            doReturn(sourceDto).when(artifactService).getCurrentTable(targetUser, firstUser, Characteristic.PHYSICAL_CONDITION);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> artifactService.useEspionage(artifactId, roomId, targetPlayerId, Characteristic.PHYSICAL_CONDITION)
            );

            assertEquals("The characteristic is already visible", exception.getMessage());
        }

        @Test
        public void shouldReturnNullWhenArtifactIdDoesNotMatch() {
            Long artifactId = 999L;
            Long roomId = 1L;
            Long targetPlayerId = 2L;
            String firstUserName = "firstUser";
            String targetUserName = "targetUser";

            ProductDTO firstUser = ProductDTO.builder()
                    .artifactRand1Id(100L)
                    .artifactRand2Id(200L)
                    .build();

            when(playerRepository.findUserNameByPlayerId(targetPlayerId)).thenReturn(Optional.of(targetUserName));
            when(authService.getCurrentUserName()).thenReturn(firstUserName);
            when(sessionService.getSession(roomId, firstUserName)).thenReturn(firstUser);

            CharacteristicShowCharacteristicRequest result = artifactService.useEspionage(
                    artifactId, roomId, targetPlayerId, Characteristic.PHYSICAL_CONDITION
            );

            assertNull(result);
            verify(sessionService, never()).getSession(roomId, targetUserName);
            verify(visibilityOfCharacteristicRepository, never()).findById(anyLong());
        }

        @Test
        public void shouldThrowEntityNotFoundWhenTargetUserNotFound() {
            Long artifactId = 100L;
            Long roomId = 1L;
            Long targetPlayerId = 2L;

            when(playerRepository.findUserNameByPlayerId(targetPlayerId)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> artifactService.useEspionage(artifactId, roomId, targetPlayerId, Characteristic.PHYSICAL_CONDITION)
            );

            assertEquals("User name is not found", exception.getMessage());
            verify(authService, never()).getCurrentUserName();
        }

        @Test
        public void shouldThrowEntityNotFoundWhenVisibilityNotFound() {
            Long artifactId = 100L;
            Long roomId = 1L;
            Long targetPlayerId = 2L;
            String firstUserName = "firstUser";
            String targetUserName = "targetUser";

            ProductDTO firstUser = ProductDTO.builder()
                    .artifactRand1Id(artifactId)
                    .artifactRand2Id(200L)
                    .build();
            ProductDTO targetUser = ProductDTO.builder()
                    .visibilityId(300L)
                    .build();

            when(playerRepository.findUserNameByPlayerId(targetPlayerId)).thenReturn(Optional.of(targetUserName));
            when(authService.getCurrentUserName()).thenReturn(firstUserName);
            when(sessionService.getSession(roomId, firstUserName)).thenReturn(firstUser);
            when(sessionService.getSession(roomId, targetUserName)).thenReturn(targetUser);
            when(visibilityOfCharacteristicRepository.findById(300L)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> artifactService.useEspionage(artifactId, roomId, targetPlayerId, Characteristic.PHYSICAL_CONDITION)
            );

            assertEquals("Visibility is not found", exception.getMessage());
        }
    }

    @Nested
    class GetCurrentTable {

        @Test
        public void shouldReturnArtifactHeroCatalogForInventory1() {
            ProductDTO targetDto = ProductDTO.builder().artifactHeroId(10L).build();
            ProductDTO userDto = ProductDTO.builder().artifactHeroId(20L).build();

            ArtifactHeroCatalog targetSource = ArtifactHeroCatalog.builder().id(10L).build();
            ArtifactHeroCatalog userSource = ArtifactHeroCatalog.builder().id(20L).build();

            when(artifactHeroCatalogRepository.findById(10L)).thenReturn(Optional.of(targetSource));
            when(artifactHeroCatalogRepository.findById(20L)).thenReturn(Optional.of(userSource));

            CharacteristicSourceDto result = artifactService.getCurrentTable(targetDto, userDto, Characteristic.INVENTORY1);

            assertNotNull(result);
            assertEquals(userSource, result.getUserCharacteristicSource());
            assertEquals(targetSource, result.getTargetCharacteristicSource());
        }

        @Test
        public void shouldReturnHeroForProfession() {
            ProductDTO targetDto = ProductDTO.builder().heroId(10L).build();
            ProductDTO userDto = ProductDTO.builder().heroId(20L).build();

            Hero targetSource = Hero.builder().id(10L).build();
            Hero userSource = Hero.builder().id(20L).build();

            when(heroRepository.findById(10L)).thenReturn(Optional.of(targetSource));
            when(heroRepository.findById(20L)).thenReturn(Optional.of(userSource));

            CharacteristicSourceDto result = artifactService.getCurrentTable(targetDto, userDto, Characteristic.PROFESSION);

            assertNotNull(result);
            assertEquals(userSource, result.getUserCharacteristicSource());
            assertEquals(targetSource, result.getTargetCharacteristicSource());
        }

        @Test
        public void shouldReturnCharacteristicPlayerForPhysicalCondition() {
            ProductDTO targetDto = ProductDTO.builder().characterId(10L).build();
            ProductDTO userDto = ProductDTO.builder().characterId(20L).build();

            CharacteristicPlayer targetSource = CharacteristicPlayer.builder().id(10L).build();
            CharacteristicPlayer userSource = CharacteristicPlayer.builder().id(20L).build();

            when(characteristicRepository.findById(10L)).thenReturn(Optional.of(targetSource));
            when(characteristicRepository.findById(20L)).thenReturn(Optional.of(userSource));

            CharacteristicSourceDto result = artifactService.getCurrentTable(targetDto, userDto, Characteristic.PHYSICAL_CONDITION);

            assertNotNull(result);
            assertEquals(userSource, result.getUserCharacteristicSource());
            assertEquals(targetSource, result.getTargetCharacteristicSource());
        }

        @Test
        public void shouldThrowEntityNotFoundWhenTargetHeroNotFound() {
            ProductDTO targetDto = ProductDTO.builder().heroId(10L).build();
            ProductDTO userDto = ProductDTO.builder().heroId(20L).build();

            when(heroRepository.findById(10L)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> artifactService.getCurrentTable(targetDto, userDto, Characteristic.PROFESSION)
            );

            assertEquals("In get Current Table PROFESSION, RASE, HOBBY not found for targetUser", exception.getMessage());
            verify(heroRepository, never()).findById(20L);
        }

        @Test
        public void shouldThrowEntityNotFoundWhenUserCharacteristicNotFound() {
            ProductDTO targetDto = ProductDTO.builder().characterId(10L).build();
            ProductDTO userDto = ProductDTO.builder().characterId(20L).build();

            CharacteristicPlayer targetSource = CharacteristicPlayer.builder().id(10L).build();

            when(characteristicRepository.findById(10L)).thenReturn(Optional.of(targetSource));
            when(characteristicRepository.findById(20L)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> artifactService.getCurrentTable(targetDto, userDto, Characteristic.STATE_OF_HEALTH)
            );

            assertEquals(" In get Current Table characteristic not found for firstUser", exception.getMessage());
        }

        @Test
        public void shouldThrowNullPointerExceptionWhenCharacteristicIsNull() {
            ProductDTO targetDto = ProductDTO.builder().build();
            ProductDTO userDto = ProductDTO.builder().build();

            assertThrows(
                    NullPointerException.class,
                    () -> artifactService.getCurrentTable(targetDto, userDto, null)
            );
        }
    }

    @Nested
    class UseStealing {

        @Test
        public void shouldStealSuccessfullyWhenCharacteristicIsAllowed() {
            Long roomId = 1L;
            Long playerId = 10L;
            Long targetPlayerId = 2L;
            String firstUserName = "currentUser";
            String targetUserName = "targetUser";

            Characteristic characteristic = Characteristic.INVENTORY1;

            ProductDTO firstUserDto = ProductDTO.builder().build();
            ProductDTO targetUserDto = ProductDTO.builder().build();

            ArtifactHeroCatalog userSource = ArtifactHeroCatalog.builder().id(100L).build();
            ArtifactHeroCatalog targetSource = ArtifactHeroCatalog.builder().id(200L).build();

            CharacteristicSourceDto sourceDto = CharacteristicSourceDto.builder()
                    .userCharacteristicSource(userSource)
                    .targetCharacteristicSource(targetSource)
                    .build();

            when(authService.getCurrentUserName()).thenReturn(firstUserName);
            when(playerRepository.findUserNameByPlayerId(targetPlayerId)).thenReturn(Optional.of(targetUserName));
            when(sessionService.getSession(roomId, firstUserName)).thenReturn(firstUserDto);
            when(sessionService.getSession(roomId, targetUserName)).thenReturn(targetUserDto);

            doReturn(sourceDto).when(artifactService).getCurrentTable(targetUserDto, firstUserDto, characteristic);
            when(roomRepository.findCodeToConnectById(roomId)).thenReturn(Optional.of("ROOM_CODE"));

            CharacteristicArtifactStealing result = artifactService.useStealing(roomId, playerId, targetPlayerId, characteristic);

            assertNotNull(result);
            assertEquals(characteristic.name(), result.getCharacteristicName());
            assertEquals(firstUserName, result.getUserName());
            assertEquals(targetUserName, result.getTargetName());

            verify(sessionService).saveSession(eq(roomId), eq(firstUserName), any(ProductDTO.class));
            verify(sessionService).saveSession(eq(roomId), eq(targetUserName), any(ProductDTO.class));
            verify(messagingTemplate).convertAndSend(eq("/topic/stealing.ROOM_CODE"), any(CharacteristicSourceDto.class));
        }

        @Test
        public void shouldThrowIllegalArgumentWhenCharacteristicIsUnstealable() {
            Long roomId = 1L;
            Long playerId = 10L;
            Long targetPlayerId = 2L;
            String targetUserName = "targetUser";

            Characteristic characteristic = Characteristic.PROFESSION;

            when(playerRepository.findUserNameByPlayerId(targetPlayerId)).thenReturn(Optional.of(targetUserName));
            when(authService.getCurrentUserName()).thenReturn("currentUser");

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> artifactService.useStealing(roomId, playerId, targetPlayerId, characteristic)
            );

            assertEquals("This characteristic can`t steal", exception.getMessage());
            verify(sessionService, never()).getSession(anyLong(), anyString());
            verify(messagingTemplate, never()).convertAndSend(anyString(),  any(CharacteristicSourceDto.class));
        }

        @Test
        public void shouldThrowEntityNotFoundWhenTargetUserNotFound() {
            Long roomId = 1L;
            Long playerId = 10L;
            Long targetPlayerId = 2L;

            when(authService.getCurrentUserName()).thenReturn("currentUser");
            when(playerRepository.findUserNameByPlayerId(targetPlayerId)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> artifactService.useStealing(roomId, playerId, targetPlayerId, Characteristic.INVENTORY1)
            );

            assertEquals("User name is not found", exception.getMessage());
            verify(sessionService, never()).getSession(anyLong(), anyString());
        }

        @Test
        public void shouldThrowEntityNotFoundWhenRoomCodeNotFound() {
            Long roomId = 1L;
            Long playerId = 10L;
            Long targetPlayerId = 2L;
            String firstUserName = "currentUser";
            String targetUserName = "targetUser";

            Characteristic characteristic = Characteristic.INVENTORY1;

            ProductDTO firstUserDto = ProductDTO.builder().build();
            ProductDTO targetUserDto = ProductDTO.builder().build();

            ArtifactHeroCatalog userSource = ArtifactHeroCatalog.builder().id(100L).build();
            ArtifactHeroCatalog targetSource = ArtifactHeroCatalog.builder().id(200L).build();

            CharacteristicSourceDto sourceDto = CharacteristicSourceDto.builder()
                    .userCharacteristicSource(userSource)
                    .targetCharacteristicSource(targetSource)
                    .build();

            when(authService.getCurrentUserName()).thenReturn(firstUserName);
            when(playerRepository.findUserNameByPlayerId(targetPlayerId)).thenReturn(Optional.of(targetUserName));
            when(sessionService.getSession(roomId, firstUserName)).thenReturn(firstUserDto);
            when(sessionService.getSession(roomId, targetUserName)).thenReturn(targetUserDto);

            doReturn(sourceDto).when(artifactService).getCurrentTable(targetUserDto, firstUserDto, characteristic);
            when(roomRepository.findCodeToConnectById(roomId)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> artifactService.useStealing(roomId, playerId, targetPlayerId, characteristic)
            );

            assertEquals("Room code not found", exception.getMessage());
        }
    }



    private Hero getTestHero(){
        return Hero.builder()
                .id(2L)
                .build();
    }
    private CharacteristicSourceDto getTestCharacteristicSourceDtoWithPlayers() {
        CharacteristicPlayer player1 = CharacteristicPlayer.builder().id(1L).build();
        CharacteristicPlayer player2 = CharacteristicPlayer.builder().id(2L).build();

        return CharacteristicSourceDto.builder()
                .userCharacteristicSource(player1)
                .targetCharacteristicSource(player2)
                .build();
    }
    private CharacteristicSourceDto getTestCharacteristicSourceDto() {
        CharacteristicPlayer player1 = CharacteristicPlayer.builder()
                .id(1L)
                .physicalCondition(PhysicalCondition.STRONG)
                .build();

        CharacteristicPlayer player2 = CharacteristicPlayer.builder()
                .id(2L)
                .physicalCondition(PhysicalCondition.WEAK)
                .build();

        return CharacteristicSourceDto.builder()
                .userCharacteristicSource(player1)
                .targetCharacteristicSource(player2)
                .build();
    }

    private ProductDTORequest getTestProductDTORequest(Long firstUserArtifactId, Long secondUserArtifactId) {
        ProductDTO user1 = ProductDTO.builder()
                .artifactRand1Id(firstUserArtifactId)
                .artifactRand2Id(firstUserArtifactId + 1)
                .build();

        ProductDTO user2 = ProductDTO.builder()
                .characterId(50L)
                .artifactRand1Id(secondUserArtifactId)
                .artifactRand2Id(secondUserArtifactId + 1)
                .build();

        return new ProductDTORequest(user1, user2);
    }

    private CharacteristicPlayer getTestCharacteristicPlayer(PhysicalCondition condition) {
        return CharacteristicPlayer.builder()
                .id(50L)
                .physicalCondition(condition)
                .psyhologicalState(PsychologicalState.STABLE)
                .figure(Figure.SLIM)
                .build();
    }
}