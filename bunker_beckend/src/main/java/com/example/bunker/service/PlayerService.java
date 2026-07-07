package com.example.bunker.service;

import com.example.bunker.dto.Hero.HeroResponse;
import com.example.bunker.dto.ProductDTO;
import com.example.bunker.model.*;
import com.example.bunker.repository.*;
import com.example.bunker.repository.VisibilityOfCharacteristicRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class PlayerService {

    public static final int MAX_NUMBER_OF_CYCLES =5;

    private final AuthService authService;
    private final SessionService sessionService;
    private final CharacteristicService characteristicService;

    private final VisibilityOfCharacteristicRepository  visibilityOfCharacteristicRepository;
    private final ArtifactRandomCatalogRepository artifactRandomCatalogRepository;
    private final ArtifactHeroCatalogRepository artifactHeroCatalogRepository;
    private final HeroRepository heroRepository;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;


    @Transactional
    public Player createPlayer(Long roomId){
        if(roomId == null||roomId<=0){
            throw new IllegalArgumentException("Room Id can't be null or empty");
        }

        String userName = authService.getCurrentUserName();

        log.info("Creating player by userName:{}",userName);

        User user= userRepository.findByUsername(userName)
               .orElseThrow(()-> new EntityNotFoundException("User not found " + userName));

        Player player= playerRepository.findByStatusAndUser(user.getId(),StatusInGame.PREPARATION_FOR_THE_GAME).orElseGet(
               ()-> buildNewPlayer(user,roomId) // тут вся логіка створення нового гравця
        );

        sessionService.updateSession(roomId, user.getUsername(), dto ->{
           dto.setCharacterId(player.getCharacter().getId());
           dto.setArtifactHeroId(player.getHero().getId());
           dto.setVisibilityId(player.getVisibilityOfCharacteristic().getId());
           dto.setProtected(false);
           dto.setStunned(false);
        });

        return playerRepository.save(player);
    }

    @Transactional
    public List<ArtifactRandomCatalog> findRandomArtifactCatalog(Long roomId){

        String userName = authService.getCurrentUserName();

        log.info("Finding random artifact catalog by user name:{}",userName);

        List<ProductDTO> sessions = sessionService.getAllSessionByRoomId(roomId);
        long numb;
        long numbCycles=0;//Лічильник ітерацій щоб при недостачі даних запобігти безкінечному циклу
        List<ArtifactRandomCatalog> artifactRandomCatalogs;
        do {
            numbCycles++;
             artifactRandomCatalogs = artifactRandomCatalogRepository.findRandomArtifact();
                   if(artifactRandomCatalogs.isEmpty()){
                       throw  new EntityNotFoundException("Artifacts not added to database or is problem with request");
                   }
           numb = artifactRandomCatalogs.stream()
                    .filter(artifact -> sessions.stream()
                            .noneMatch(session ->
                                    artifact.getId().equals(session.getArtifactRand1Id()) ||
                                            artifact.getId().equals(session.getArtifactRand2Id())
                            )
                    )
                    .count();
            if(numbCycles>MAX_NUMBER_OF_CYCLES){
                throw new NoResultException("Maximum number of cycles reached because most of all RANDOM ARTIFACTS have been busy");
            }
            if(numbCycles>MAX_NUMBER_OF_CYCLES/2){
                log.warn("In findRandomArtifactCatalog is so many cycles");
            }
        }while (numb == 0);
        return  artifactRandomCatalogs;
    }

    @Transactional
    public ArtifactHeroCatalog addHeroArtifacts(Long roomId){
        String userName = authService.getCurrentUserName();

        log.info("Creating Hero artifacts by userName:{}",userName);

        ProductDTO userData = sessionService.getSession(roomId, userName);
        List<ProductDTO>  sessions = sessionService.getAllSessionByRoomId(roomId);

        long numb;
        long numbCycles=0;//Лічильник ітерацій щоб при недостачі даних запобігти безкінечному циклу
        ArtifactHeroCatalog artifactHeroCatalogResalt;
        do{
            numbCycles++;

            ArtifactHeroCatalog artifactHeroCatalog = artifactHeroCatalogRepository.findHeroArtifact()
                   .orElseThrow(()-> new EntityNotFoundException("Artifacts not added to database or problem with request by userName: " + userName));

            artifactHeroCatalogResalt=artifactHeroCatalog;
           numb= sessions.stream()
                   .filter(o -> o.getArtifactHeroId().equals(artifactHeroCatalog.getId()) )
                   .count();

            if(numbCycles>MAX_NUMBER_OF_CYCLES){
                throw new NoResultException("Maximum number of cycles reached because most of all HERO ARTIFACTS have been busy");
            }
            if(numbCycles>MAX_NUMBER_OF_CYCLES/2){
                log.warn("In addHeroArtifacts is so many cycles");
            }

        }while (numb > 0);


        if(userData.getPlayerId() == null){
            ArtifactHeroCatalog finalArtifactHeroCatalogResalt = artifactHeroCatalogResalt;

            sessionService.updateSession(roomId,userName, dto ->{
                dto.setArtifactHeroId(finalArtifactHeroCatalogResalt.getId());
            });
        }
        Player player = playerRepository.findById(userData.getPlayerId())
                .orElseThrow(()-> new EntityNotFoundException("Data in Redis was damaged"));


        if(!userData.getArtifactHeroId().equals(player.getArtifactHeroCatalog().getId())){
             throw new IllegalArgumentException("In Redis data playerHeroArtifact is not correct " +
                     "without null or " + artifactHeroCatalogResalt.getId() +
                     " in Redis containing "+ userData.getArtifactHeroId());
        }

        player.setArtifactHeroCatalog(artifactHeroCatalogResalt);

        sessionService.updateSession(roomId,userName, dto ->{
            dto.setArtifactHeroId(player.getArtifactHeroCatalog().getId());
        });
        playerRepository.save(player);

        return artifactHeroCatalogResalt;

    }

    @Transactional
    public void addTwoRandomArtifacts(Long id1, Long id2, Long roomId){
        String userName = authService.getCurrentUserName();

        ProductDTO userData = sessionService.getSession(roomId, userName);

        log.info("Adding two artifacts by userName:{}",userName);
       sessionService.updateSession(roomId,userName,dto->{
           dto.setArtifactRand1Id(id1);
           dto.setArtifactRand2Id(id2);
       });

       Player player = playerRepository.findById(userData.getPlayerId()).orElseThrow(
               ()-> new EntityNotFoundException("Data in Redis was damaged") );
       List<ArtifactRandomCatalog> randomCatalogs = artifactRandomCatalogRepository.findByIds(id1,id2);

       if(randomCatalogs.isEmpty()){
           throw new EntityNotFoundException("Artifacts not added to database or problem with request");
       }

       player.setFirstArtifactRandomCatalog(randomCatalogs.get(0));
       player.setSecondArtifactRandomCatalog(randomCatalogs.get(1));

       playerRepository.save(player);

    }

    public HeroResponse addHero(Long roomId){

        String userName = authService.getCurrentUserName();

        log.info("Creating Hero  by userName:{}",userName);

        List<ProductDTO>  sessions = sessionService.getAllSessionByRoomId(roomId);
        long numb;
        Hero realHero;
        int numbCycles=0;
        do{
            numbCycles++;
            Hero hero = heroRepository.findHero().orElseThrow(()-> new EntityNotFoundException("hero not added to database or request had bug"));

             numb =sessions.stream()
                    .filter(o -> o.getHeroId().equals(hero.getId()))
                    .count();
             realHero=hero;
             if(numbCycles>MAX_NUMBER_OF_CYCLES/2){
                 log.warn("In addHero is so many cycles");
             }
             if(numbCycles>MAX_NUMBER_OF_CYCLES){
                 throw new RuntimeException("Maximum number of cycles reached because most of all HERO have been busy");
             }
        }while (numb>0);

        Hero finalRealHero= heroRepository.save(realHero);

        sessionService.updateSession(roomId,userName, dto->{
            dto.setHeroId(finalRealHero.getId());
        });
        return new HeroResponse(finalRealHero);
    }

    private Player buildNewPlayer(User user,Long roomId) {

        ArtifactHeroCatalog artifactHeroCatalog;
        List<ProductDTO> productsDTO= sessionService.getAllSessionByRoomId(roomId);
        long numb;
        do{
         artifactHeroCatalog = artifactHeroCatalogRepository
                .findHeroArtifact()
                .orElseThrow(() -> new EntityNotFoundException("Hero artifact not added or problem with request"));

            Long currentArtifactId = artifactHeroCatalog.getId();

            numb =productsDTO.stream()
                 .filter(o->o.getArtifactHeroId() != null && o.getArtifactHeroId().equals(currentArtifactId))
                 .count();
        }while (numb>0);

        CharacteristicPlayer characteristicPlayer;
        do {
            characteristicPlayer = characteristicService.createCharacteristic(roomId);

            Long currentCharacteristicId = characteristicPlayer.getId();

            numb =productsDTO.stream()
                    .filter(o ->o.getCharacterId()!=null && o.getCharacterId().equals(currentCharacteristicId))
                    .count();

        }while (numb>0);

        VisibilityOfCharacteristic visibilityOfCharacteristic = new VisibilityOfCharacteristic();
        VisibilityOfCharacteristic visibility = visibilityOfCharacteristicRepository.save(visibilityOfCharacteristic);


        return Player.builder()
                .user(user)
                .artifactHeroCatalog(artifactHeroCatalog)
                .character(characteristicPlayer)
                .createdAt(LocalDateTime.now())
                .visibilityOfCharacteristic(visibility)
                .status(StatusInGame.PREPARATION_FOR_THE_GAME)
                .build();
    }
}
