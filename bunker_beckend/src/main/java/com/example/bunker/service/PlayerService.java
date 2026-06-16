package com.example.bunker.service;

import com.example.bunker.dto.ProductDTO;
import com.example.bunker.model.*;
import com.example.bunker.repository.*;
import com.example.bunker.repository.VisibilityOfCharacteristicRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PlayerService {

    private final AuthService authService;
    private final SessionService sessionService;

    private final VisibilityOfCharacteristicRepository  visibilityOfCharacteristicRepository;
    private final ArtifactRandomCatalogRepository artifactRandomCatalogRepository;
    private final ArtifactHeroCatalogRepository artifactHeroCatalogRepository;
    private final CharacteristicRepository  characteristicRepository;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;


    public Player createPlayer(Long roomId){
       User user= userRepository.findByEmail(authService.getCurrentUserEmail())
               .orElseThrow(()-> new EntityNotFoundException("User not found"));

       Player player= playerRepository.findByStatusAndUser(user.getId(),StatusInGame.PREPARATION_FOR_THE_GAME).orElseGet(
               ()-> buildNewPlayer(user,roomId) // тут вся логіка створення нового гравця
       );

       sessionService.updateSession(roomId, user.getEmail(), dto ->{
           dto.setCharacterId(player.getCharacter().getId());
           dto.setArtifactHeroId(player.getHero().getId());
       });

       return playerRepository.save(player);
    }


    public List<ArtifactRandomCatalog> findRandomArtifactCatalog(Long roomId){
        List<ProductDTO> sessions = sessionService.getAllSessionByRoomId(roomId);
        long numb;
        List<ArtifactRandomCatalog> artifactRandomCatalogs;
        do {
             artifactRandomCatalogs = artifactRandomCatalogRepository.findRandomArtifact()
                    .orElseThrow(()-> new EntityNotFoundException("Artifacts not added to database or problem with request"));

           numb = artifactRandomCatalogs.stream()
                    .filter(artifact -> sessions.stream()
                            .noneMatch(session ->
                                    artifact.getId().equals(session.getArtifactRand1Id()) ||
                                            artifact.getId().equals(session.getArtifactRand2Id())
                            )
                    )
                    .count();
        }while (numb>0);
        return  artifactRandomCatalogs;
    }
    public ArtifactHeroCatalog addHeroArtifacts(Long roomId){
        String userEmail = authService.getCurrentUserEmail();
        ProductDTO userData = sessionService.getSession(roomId, userEmail);
        List<ProductDTO>  sessions = sessionService.getAllSessionByRoomId(roomId);

        long numb;
        ArtifactHeroCatalog artifactHeroCatalogResalt;
        do{
            ArtifactHeroCatalog artifactHeroCatalog = artifactHeroCatalogRepository.findHeroArtifact()
                   .orElseThrow(()-> new EntityNotFoundException("Artifacts not added to database or problem with request"));

            artifactHeroCatalogResalt=artifactHeroCatalog;
           numb= sessions.stream()
                   .filter(o -> o.getArtifactHeroId().equals(artifactHeroCatalog.getId()) )
                   .count();

        }while (numb>0);

        Player player = playerRepository.findById(Math.toIntExact(userData.getPlayerId()))
                .orElseThrow(()-> new EntityNotFoundException("Data in Redis was damaged"));


        if(userData.getPlayerId() == null){
            ArtifactHeroCatalog finalArtifactHeroCatalogResalt = artifactHeroCatalogResalt;

            sessionService.updateSession(roomId,userEmail, dto ->{
                dto.setArtifactHeroId(finalArtifactHeroCatalogResalt.getId());
            });
        }

        if(!userData.getArtifactHeroId().equals(player.getArtifactHeroCatalog().getId())){
             throw new IllegalArgumentException("In Redis data playerHeroArtifact is not correct " +
                     "without null or " + artifactHeroCatalogResalt.getId() +
                     " in Redis containing "+ userData.getArtifactHeroId());
        }

        player.setArtifactHeroCatalog(artifactHeroCatalogResalt);
        playerRepository.save(player);

        return artifactHeroCatalogResalt;

    }



    public void addTwoArtifacts(Long id1, Long id2,Long roomId){

        ProductDTO userData = sessionService.getSession(roomId, authService.getCurrentUserEmail());

        String email = authService.getCurrentUserEmail();
       sessionService.updateSession(roomId,email,dto->{
           dto.setArtifactRand1Id(id1);
           dto.setArtifactRand2Id(id2);
       });

       Player player = playerRepository.findById(Math.toIntExact(userData.getPlayerId())).orElseThrow(
               ()-> new EntityNotFoundException("Data in Redis was damaged") );
       List<ArtifactRandomCatalog> randomCatalogs = artifactRandomCatalogRepository.findByIds(id1,id2)
               .orElseThrow(()-> new EntityNotFoundException("Artifacts not added to database or problem with request"));
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
