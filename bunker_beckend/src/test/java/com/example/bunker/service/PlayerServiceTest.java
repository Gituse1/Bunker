package com.example.bunker.service;

import com.example.bunker.dto.Hero.HeroResponse;
import com.example.bunker.dto.ProductDTO;
import com.example.bunker.model.*;
import com.example.bunker.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {
    @Mock
    private AuthService authService;

    @Mock
    private SessionService sessionService;

    @Mock
    private CharacteristicService characteristicService;

    @Mock
    private VisibilityOfCharacteristicRepository visibilityOfCharacteristicRepository;

    @Mock
    private ArtifactRandomCatalogRepository artifactRandomCatalogRepository;

    @Mock
    private ArtifactHeroCatalogRepository artifactHeroCatalogRepository;

    @Mock
    private HeroRepository heroRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PlayerService playerService;

    @BeforeEach
    void beforeEach() {
        when(authService.getCurrentUserName()).thenReturn("user1");

    }

    @Nested
    class CreatePlayer {

        @Test
        public void shouldThrowWhenUserNotFound() {
            when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> playerService.createPlayer(1L)
            );
            assertEquals("User not found user1", exception.getMessage());
            verify(playerRepository,never()).findByStatusAndUser(any(), any(StatusInGame.class));
            verify(sessionService,never()).updateSession(any(),anyString(),any());
            verify(playerRepository,never()).save(any());
        }

        @Test
        public void shouldCreateNewPlayerWhenNoPlayerInPreparationStatus() {
            Long roomId = 1L;
            User user = getTestUser();

            ArtifactHeroCatalog artifactHeroCatalog = ArtifactHeroCatalog.builder()
                    .id(10L)
                    .build();

            CharacteristicPlayer characteristicPlayer = CharacteristicPlayer.builder()
                    .id(20L)
                    .build();

            VisibilityOfCharacteristic savedVisibility = VisibilityOfCharacteristic.builder()
                    .id(30L)
                    .build();

            when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
            when(playerRepository.findByStatusAndUser(user.getId(), StatusInGame.PREPARATION_FOR_THE_GAME))
                    .thenReturn(Optional.empty());

            when(sessionService.getAllSessionByRoomId(roomId)).thenReturn(Collections.emptyList());

            when(artifactHeroCatalogRepository.findHeroArtifact()).thenReturn(Optional.of(artifactHeroCatalog));
            when(characteristicService.createCharacteristic(roomId)).thenReturn(characteristicPlayer);
            when(visibilityOfCharacteristicRepository.save(any(VisibilityOfCharacteristic.class)))
                    .thenReturn(savedVisibility);

            when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));

            playerService.createPlayer(roomId);

            ArgumentCaptor<Player> captor = ArgumentCaptor.forClass(Player.class);
            verify(playerRepository, atLeastOnce()).save(captor.capture());

            Player savedPlayer = captor.getValue();
            assertEquals(user.getId(), savedPlayer.getUser().getId());
            assertEquals(artifactHeroCatalog.getId(), savedPlayer.getArtifactHeroCatalog().getId());
            assertEquals(characteristicPlayer.getId(), savedPlayer.getCharacter().getId());
            assertEquals(savedVisibility.getId(), savedPlayer.getVisibilityOfCharacteristic().getId());
            assertEquals(StatusInGame.PREPARATION_FOR_THE_GAME, savedPlayer.getStatus());
        }

        @Test
        public void shouldUpdateSessionWithCorrectData() {
            Long roomId = 1L;
            User user = getTestUser();
            Player player = getTestPlayer();

            when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
            when(playerRepository.findByStatusAndUser(user.getId(), StatusInGame.PREPARATION_FOR_THE_GAME))
                    .thenReturn(Optional.of(player));
            when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));

            playerService.createPlayer(roomId);

            verify(sessionService).updateSession(eq(roomId), eq(user.getUsername()), any());
            
        }


        @Test
        public void createPlayerByIncorrectRoomId(){

            Long roomId = 1L;

            User user = getTestUser();
            Player player = getTestPlayer();

            when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
            when(playerRepository.findByStatusAndUser(any(Long.class),any(StatusInGame.class)))
                    .thenReturn(Optional.of(player));
            doNothing().when(sessionService).updateSession(any(),anyString(),any());

            playerService.createPlayer(roomId);

            verify(playerRepository).save(any(Player.class));

        }

    }

    @Nested
    class FindRandomArtifactCatalog{

        @Test
        public void findRandomArtifactCatalogByIncorrectRandomArtifactId(){
            Long roomId = 1L;
            List<ProductDTO> testProductsDtoList = getTestListProductDTO();
            List<ArtifactRandomCatalog> testRandomArtifactCatalog = getTestListRandomArtifactCatalog();

            when(sessionService.getAllSessionByRoomId(anyLong())).thenReturn(testProductsDtoList);
            when(artifactRandomCatalogRepository.findRandomArtifact()).thenReturn(testRandomArtifactCatalog);


            NoResultException exception = assertThrows(
                    NoResultException.class,()->playerService.findRandomArtifactCatalog(roomId));

            assertNotNull(exception);
            verify(artifactRandomCatalogRepository,atLeast(3)).findRandomArtifact();
        }
        @Test
        public void findRandomArtifactCatalogByEmptyRandomArtifact(){
            Long roomId = 1L;
            List<ProductDTO> testProductsDtoList = getTestListProductDTO();

            when(sessionService.getAllSessionByRoomId(anyLong())).thenReturn(testProductsDtoList);
            when(artifactRandomCatalogRepository.findRandomArtifact()).thenReturn(Collections.emptyList());


            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,()->playerService.findRandomArtifactCatalog(roomId));

            assertNotNull(exception);
        }
    }

    @Nested
    class AddHeroArtifactCatalog {
        @Test
        public void findHeroArtifactCatalogByIncorrectHeroArtifactId(){
            Long roomId = 1L;
            ArtifactHeroCatalog testHeroArtifactCatalog = getTestHeroArtifactCatalog();
            ProductDTO testProduct = getTestProductDTO();
            List<ProductDTO> testListProductsDto = getTestListProductDTO();
            Player testPlayer = getTestPlayer();

            when(sessionService.getSession(anyLong(),anyString())).thenReturn(testProduct);
            when(sessionService.getAllSessionByRoomId(anyLong())).thenReturn(testListProductsDto);
            when(artifactHeroCatalogRepository.findHeroArtifact()).thenReturn(Optional.of(testHeroArtifactCatalog));

            lenient().when(playerRepository.findById(anyLong())).thenReturn(Optional.of(testPlayer));
            lenient().doNothing().when(sessionService).updateSession(any(),anyString(),any());
            lenient().doNothing().when(sessionService).deleteSession(anyLong(),anyString());

            NoResultException exception = assertThrows(
                    NoResultException.class,()->playerService.addHeroArtifacts(roomId));

            verify(artifactHeroCatalogRepository,atLeast(3)).findHeroArtifact();
            verify(sessionService,times(0)).updateSession(any(),anyString(),any());
            verify(sessionService,times(0)).deleteSession(any(),anyString());
            assertNotNull(exception);

        }
        @Test
        public void findHeroArtifactCatalogByEmptyHeroArtifactId(){
            Long roomId = 1L;
            ProductDTO testProduct = getTestProductDTO();
            List<ProductDTO> testListProductsDto = getTestListProductDTO();
            Player testPlayer = getTestPlayer();

            when(sessionService.getSession(anyLong(),anyString())).thenReturn(testProduct);
            when(sessionService.getAllSessionByRoomId(anyLong())).thenReturn(testListProductsDto);
            when(artifactHeroCatalogRepository.findHeroArtifact()).thenReturn(Optional.empty());

            lenient().when(playerRepository.findById(anyLong())).thenReturn(Optional.of(testPlayer));
            lenient().doNothing().when(sessionService).updateSession(any(),anyString(),any());
            lenient().doNothing().when(sessionService).deleteSession(anyLong(),anyString());

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,()->playerService.addHeroArtifacts(roomId));

            verify(artifactHeroCatalogRepository,times(1)).findHeroArtifact();
            verify(sessionService,times(0)).updateSession(any(),anyString(),any());
            verify(sessionService,times(0)).deleteSession(any(),anyString());
            assertNotNull(exception);

        }

    }

    @Nested
    class AddTwoRandomArtifacts{
        @Test
        public void findTwoRandomArtifactCatalogByIncorrectRandomArtifactId(){
            Long id1 = 1L;
            Long id2 = 2L;
            Long roomId = 100L;
            String userName = "testUser";

            ProductDTO testProduct = getTestProductDTO();
            Player player = getTestPlayer();

            when(authService.getCurrentUserName()).thenReturn(userName);
            when(sessionService.getSession(roomId, userName)).thenReturn(testProduct);
            doNothing().when(sessionService).updateSession(any(), anyString(), any());
            when(playerRepository.findById(testProduct.getPlayerId())).thenReturn(Optional.of(player));
            when(artifactRandomCatalogRepository.findByIds(id1, id2)).thenReturn(Collections.emptyList());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> playerService.addTwoRandomArtifacts(id1, id2, roomId));

            assertEquals("Artifacts not added to database or problem with request", ex.getMessage());
            verify(playerRepository, never()).save(any());
        }
    }

    @Nested
    class AddHero {

        @Test
        public void shouldAddHeroSuccessfullyWhenNoCollision() {
            Long roomId = 1L;
            Hero testHero = getTestHero();

            when(sessionService.getAllSessionByRoomId(roomId)).thenReturn(Collections.emptyList());
            when(heroRepository.findHero()).thenReturn(Optional.of(testHero));
            when(heroRepository.save(any(Hero.class))).thenAnswer(inv -> inv.getArgument(0));
            doNothing().when(sessionService).updateSession(eq(roomId), anyString(), any());

            HeroResponse response = playerService.addHero(roomId);

            assertNotNull(response);
            verify(heroRepository, times(1)).findHero();
            verify(heroRepository, times(1)).save(testHero);
            verify(sessionService, times(1)).updateSession(eq(roomId), anyString(), any());
        }

        @Test
        public void shouldThrowEntityNotFoundWhenHeroNotFound() {
            Long roomId = 1L;

            when(sessionService.getAllSessionByRoomId(roomId)).thenReturn(Collections.emptyList());
            when(heroRepository.findHero()).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> playerService.addHero(roomId)
            );

            assertEquals("hero not added to database or request had bug", exception.getMessage());
            verify(heroRepository, never()).save(any());
            verify(sessionService, never()).updateSession(anyLong(), anyString(), any());
        }

        @Test
        public void shouldThrowRuntimeExceptionWhenMaxCyclesReached() {
            Long roomId = 1L;
            Hero testHero = getTestHero();

            ProductDTO sessionWithHero = ProductDTO.builder()
                    .heroId(testHero.getId())
                    .build();
            List<ProductDTO> sessions = Collections.singletonList(sessionWithHero);

            when(sessionService.getAllSessionByRoomId(roomId)).thenReturn(sessions);
            when(heroRepository.findHero()).thenReturn(Optional.of(testHero));

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> playerService.addHero(roomId)
            );

            assertEquals("Maximum number of cycles reached because most of all HERO have been busy", exception.getMessage());
            verify(heroRepository, times(6)).findHero();
            verify(heroRepository, never()).save(any());
            verify(sessionService, never()).updateSession(anyLong(), anyString(), any());
        }
    }

    public ProductDTO getTestProductDTO(){
        return ProductDTO.builder()
                .playerId(1L)
                .artifactHeroId(1L)
                .build();

    }

    public ArtifactHeroCatalog getTestHeroArtifactCatalog(){

        return ArtifactHeroCatalog.builder()
                .id(1L)
                .build();

    }
    public List<ArtifactRandomCatalog> getTestListRandomArtifactCatalog(){
        List<ArtifactRandomCatalog> testProductsDtoList = new ArrayList<>();
        for( long i = 1; i < 3; i++){

            ArtifactRandomCatalog artifactRandomCatalog = ArtifactRandomCatalog.builder()
                    .id(i)
                    .build();
            testProductsDtoList.add(artifactRandomCatalog);
        }
        return testProductsDtoList;
    }

    public List<ProductDTO> getTestListProductDTO(){
        List<ProductDTO> productDTOList = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        for(long i=1,j=1;i<13;i+=2,j++){
            ProductDTO productDTO = ProductDTO.builder()
                    .artifactRand1Id(i)
                    .artifactRand2Id(i+1)
                    .artifactHeroId(j)
                    .build();
            productDTOList.add(productDTO);

        }
        return productDTOList;
    }

    public User getTestUser(){
        return User.builder()
                .id(1L)
                .email("testEmail@gamil.com")
                .createDate(LocalDateTime.now())
                .lastVisit(LocalDateTime.now())
                .password("cedwdfd")
                .username("user1")
                .build();

    }

    public Player getTestPlayer(){
        List<ArtifactRandomCatalog> testList = getTestListRandomArtifactCatalog();
        return Player.builder()
                .id(1L)
                .user(getTestUser())
                .visibilityOfCharacteristic(getTestVisibilityOfCharacteristicPlayer())
                .character(getTestCharacteristicPlayer())
                .artifactHeroCatalog(getTestHeroArtifactCatalog())
                .firstArtifactRandomCatalog(testList.get(0))
                .secondArtifactRandomCatalog(testList.get(1))
                .hero(getTestHero())
                .build();
    }

    public VisibilityOfCharacteristic getTestVisibilityOfCharacteristicPlayer(){
        return VisibilityOfCharacteristic.builder()
                .id(2L)
                .build();
    }

    public CharacteristicPlayer getTestCharacteristicPlayer(){
        return CharacteristicPlayer.builder()
                .id(2L)
                .build();
    }
    public Hero getTestHero(){
        return Hero.builder()
                .id(2L)
                .build();
    }

}