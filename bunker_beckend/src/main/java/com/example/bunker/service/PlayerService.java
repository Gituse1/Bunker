package com.example.bunker.service;

import com.example.bunker.dto.ProductDTO;
import com.example.bunker.model.*;
import com.example.bunker.repository.*;
import com.example.bunker.repository.VisibilityOfCharacteristicRepository;
import jakarta.persistence.EntityNotFoundException;
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

    public static final int MAX_NUMBER_OF_CYCLES =20;

    private final AuthService authService;
    private final SessionService sessionService;

    private final VisibilityOfCharacteristicRepository  visibilityOfCharacteristicRepository;
    private final ArtifactRandomCatalogRepository artifactRandomCatalogRepository;
    private final ArtifactHeroCatalogRepository artifactHeroCatalogRepository;
    private final CharacteristicRepository  characteristicRepository;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;


    @Transactional
    public Player createPlayer(Long roomId){
        String userName = authService.getCurrentUserName();

        log.info("Creating player by userName:{}",userName);

        User user= userRepository.findByUsername(userName)
               .orElseThrow(()-> new EntityNotFoundException("User not found" + userName));

        Player player= playerRepository.findByStatusAndUser(user.getId(),StatusInGame.PREPARATION_FOR_THE_GAME).orElseGet(
               ()-> buildNewPlayer(user,roomId) // тут вся логіка створення нового гравця
        );

        sessionService.updateSession(roomId, user.getUsername(), dto ->{
           dto.setCharacterId(player.getCharacter().getId());
           dto.setArtifactHeroId(player.getHero().getId());
        });

        return playerRepository.save(player);
    }


    public List<ArtifactRandomCatalog> findRandomArtifactCatalog(Long roomId){
        List<ProductDTO> sessions = sessionService.getAllSessionByRoomId(roomId);
        long numb;
        long numbCycles=0;//Лічильник ітерацій щоб при недостачі даних запобігти безкінечному циклу
        List<ArtifactRandomCatalog> artifactRandomCatalogs;
        do {
            numbCycles++;
            if(numbCycles>MAX_NUMBER_OF_CYCLES){
                throw new RuntimeException("Maximum number of cycles reached because most of all RANDOM ARTIFACTS have been busy");
            }
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
        }while (numb == 0);
        return  artifactRandomCatalogs;
    }
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
                throw new RuntimeException("Maximum number of cycles reached because most of all HERO ARTIFACTS have been busy");
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
        playerRepository.save(player);

        return artifactHeroCatalogResalt;

    }

    @Transactional
    public void addTwoArtifacts(Long id1, Long id2,Long roomId){


        ProductDTO userData = sessionService.getSession(roomId, authService.getCurrentUserName());
        String userName = authService.getCurrentUserName();

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
             characteristicPlayer = characteristicRepository
                    .findRandomArtifact()
                    .orElseThrow(() -> new EntityNotFoundException("Characteristic not added or problem with request"));

            Long currentCharacteristicId = characteristicPlayer.getId();

            numb =productsDTO.stream()
                    .filter(o ->o.getCharacterId()!=null && o.getCharacterId().equals(currentCharacteristicId))
                    .count();

        }while (numb>0);

        VisibilityOfCharacteristic visibilityOfCharacteristic = new VisibilityOfCharacteristic();
        VisibilityOfCharacteristic visibility = visibilityOfCharacteristicRepository.save(visibilityOfCharacteristic);


        return playerRepository.save(Player.builder()
                .user(user)
                .artifactHeroCatalog(artifactHeroCatalog)
                .character(characteristicPlayer)
                .createdAt(LocalDateTime.now())
                .visibilityOfCharacteristic(visibility)
                .status(StatusInGame.PREPARATION_FOR_THE_GAME)
                .build());
    }
}
