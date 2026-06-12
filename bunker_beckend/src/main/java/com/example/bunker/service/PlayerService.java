package com.example.bunker.service;

import com.example.bunker.model.*;
import com.example.bunker.repository.*;
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

    private final ArtifactRandomCatalogRepository artifactRandomCatalogRepository;
    private final ArtifactHeroCatalogRepository artifactHeroCatalogRepository;
    private final CharacteristicRepository  characteristicRepository;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;



    public Player createPlayer(Long roomId){
       User user= userRepository.findByEmail(authService.getCurrentUserName())
               .orElseThrow(()-> new EntityNotFoundException("User not found"));

       Player player= playerRepository.findByStatusAndUser(user.getId(),StatusInGame.PREPARATION_FOR_THE_GAME).orElseGet(
               ()-> buildNewPlayer(user,roomId)
       );

       String email= authService.getCurrentUserName();
       sessionService.updateSession(roomId,email,dto ->{
           dto.setCharacterId(player.getCharacter().getId());
           dto.setArtifactHeroId(player.getHero().getId());
       });

       return player;
    }

    public List<ArtifactRandomCatalog> findRandomArtifactCatalog(){
        return artifactRandomCatalogRepository.findRandomArtifact()
                .orElseThrow(()-> new EntityNotFoundException("Artifacts not added to database or problem with request"));
    }


    private Player buildNewPlayer(User user,Long roomId) {
        ArtifactHeroCatalog artifactHeroCatalog = artifactHeroCatalogRepository
                .findHeroArtifact()
                .orElseThrow(() -> new EntityNotFoundException("Hero artifact not added or problem with request"));
        CharacteristicPlayer characteristicPlayer = characteristicRepository
                .findRandomArtifact()
                .orElseThrow(() -> new EntityNotFoundException("Characteristic not added or problem with request"));

        return playerRepository.save(Player.builder()
                .user(user)
                .artifactHeroCatalog(artifactHeroCatalog)
                .character(characteristicPlayer)
                .createdAt(LocalDateTime.now())
                .status(StatusInGame.PREPARATION_FOR_THE_GAME)
                .build());
    }
}
