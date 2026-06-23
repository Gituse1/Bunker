package com.example.bunker.service;

import com.example.bunker.dto.ProductDTO;
import com.example.bunker.dto.Room.RoomDataResponse;
import com.example.bunker.model.Player;
import com.example.bunker.model.Room;
import com.example.bunker.model.RoomPlayer;
import com.example.bunker.model.StatusInGame;
import com.example.bunker.projection.PlayerProjection;
import com.example.bunker.repository.PlayerRepository;
import com.example.bunker.repository.RoomPlayerRepository;
import com.example.bunker.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomPlayerService {

    private final RoomPlayerRepository roomPlayerRepository;
    private final RoomRepository roomRepository;
    private final PlayerRepository  playerRepository;

    private final PlayerService  playerService;
    private final SessionService  sessionService;

    private final AuthService authService;

    @Transactional
    public RoomDataResponse connectToGame(String codeToConnect) {

        String userName =authService.getCurrentUserName();

        log.info("user : "+userName+"connectToGame by code : "+codeToConnect);


        Room room =roomRepository.findRoomByCodeToConnect(codeToConnect).orElseThrow(
                ()->new EntityNotFoundException("Code is not valid or something went wrong"));


        //Шукаємо чи вже існує створювався герой до цієї кімнати.
        RoomPlayer roomPlayer =roomPlayerRepository.findPlayerByRoomPlayerId(room.getId(),userName)
                .orElseGet(() ->{           //Якщо не знайшли то створюємо нового героя
                    RoomPlayer newRoomPlayer = createRoomPlayer(room);
                    newRoomPlayer.setPlayer(playerService.createPlayer(room.getId()));
                    return newRoomPlayer;
                });

        // Якщо витягнули вже існуючого
        if(roomPlayer.getPlayer().getStatus().equals(StatusInGame.WAS_LEFT_EARLIER)){
            roomPlayer.getPlayer().setStatus(StatusInGame.PREPARATION_FOR_THE_GAME);
        }

        //Шукаємо інших гравців з кімнати
        List<PlayerProjection> projections = getUsersNameByRoomId(room.getId());

        List<String> names = new ArrayList<>();
        List<Long> idUser = new ArrayList<>();

        for(PlayerProjection projection:projections){
            names.add(projection.getName());
            idUser.add(projection.getPlayerId());
        }

        //Зберігаємо зміни.
        roomPlayerRepository.save(roomPlayer);

        sessionService.updateSession(roomPlayer.getRoom().getId(),userName,
                preparingRequest(roomPlayer));

        return  RoomDataResponse.builder()
                .names(names)
                .roomId(room.getId())
                .player(roomPlayer.getPlayer())
                .ids(idUser)
                .build();
    }

    @Transactional
    public void leaveGame(Long roomPlayerId) {
        String name =authService.getCurrentUserName();

        log.info("user : "+name+"leaveGame");
       RoomPlayer roomPlayer= roomPlayerRepository.findByIdCurrentRoomPlayer(roomPlayerId,name).orElseThrow(
               ()->new IllegalArgumentException("Player is not exist"));

       if(roomPlayer.getPlayer().getStatus().equals(StatusInGame.PREPARATION_FOR_THE_GAME)){
           sessionService.deleteSession(roomPlayer.getRoom().getId(),name);

           playerRepository.deleteById(roomPlayer.getPlayer().getId());
           roomPlayerRepository.deleteById(roomPlayerId);
           return;
       }
       roomPlayer.getPlayer().setStatus(StatusInGame.WAS_LEFT_EARLIER);
       roomPlayer.setJoined(false);

       roomPlayerRepository.save(roomPlayer);
        playerRepository.deleteById(roomPlayer.getPlayer().getId());
    }

    @Transactional
    public RoomPlayer createRoomPlayer(Room room){

        RoomPlayer roomPlayer = RoomPlayer.builder()
                .room(room)
                .build();
       return roomPlayerRepository.save(roomPlayer);
    }

    private List<PlayerProjection> getUsersNameByRoomId(Long roomId){
        List<PlayerProjection> projection= roomPlayerRepository.findUserNameByRoomId(roomId);
        if(projection.isEmpty()){
            throw new IllegalArgumentException("Players is not found but code is valid");
        };
        return projection;
    }



    private Consumer<ProductDTO> preparingRequest(RoomPlayer roomPlayer){
        Player player = roomPlayer.getPlayer();
        return dto -> {
            dto.setPlayerId(player.getId());

            if(player.getHero()!=null){
                dto.setHeroId(player.getHero().getId());
            }

            if(player.getCharacter()!=null){
                dto.setCharacterId(player.getCharacter().getId());
            }

            if(player.getArtifactHeroCatalog()!=null){
                dto.setArtifactHeroId(player.getArtifactHeroCatalog().getId());
            }

            if(player.getFirstArtifactRandomCatalog()!=null){
                dto.setArtifactRand1Id(player.getFirstArtifactRandomCatalog().getId());
            }
            if(player.getSecondArtifactRandomCatalog()!=null){
                dto.setArtifactRand2Id(player.getSecondArtifactRandomCatalog().getId());
            }
        };
    }
}
