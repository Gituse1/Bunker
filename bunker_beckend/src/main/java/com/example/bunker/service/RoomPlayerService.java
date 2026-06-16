package com.example.bunker.service;

import com.example.bunker.dto.ProductDTO;
import com.example.bunker.dto.Room.RoomDataRequest;
import com.example.bunker.model.Player;
import com.example.bunker.model.Room;
import com.example.bunker.model.RoomPlayer;
import com.example.bunker.model.StatusInGame;
import com.example.bunker.projection.PlayerProjection;
import com.example.bunker.repository.RoomPlayerRepository;
import com.example.bunker.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class RoomPlayerService {

    private final RoomPlayerRepository roomPlayerRepository;
    private final RoomRepository roomRepository;
    private final PlayerService  playerService;
    private final SessionService  sessionService;

    private final AuthService authService;

    @Transactional
    public RoomDataRequest connectToGame(String codeToConnect) {
        Room room =roomRepository.findRoomByCode(codeToConnect).orElseThrow(
                ()->new RuntimeException("Code is not valid or something went wrong"));

        String userName =authService.getCurrentUserEmail();

        //Знаходимо свого існуючого героя
        RoomPlayer roomPlayer =roomPlayerRepository.findPlayerByRoomPlayerId(room.getId(),userName)
                .orElseGet(() ->createRoomPlayer(room));

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

        return  RoomDataRequest.builder()
                .names(names)
                .roomId(roomPlayer.getId())
                .player(roomPlayer.getPlayer())
                .ids(idUser)
                .build();
    }

    @Transactional
    public void leaveGame(Long id) {
        String name =authService.getCurrentUserEmail();
       RoomPlayer roomPlayer= roomPlayerRepository.findByIdCurrentUser(id,name).orElseThrow(
               ()->new RuntimeException("Player is not exist"));

       roomPlayer.getPlayer().setStatus(StatusInGame.WAS_LEFT_EARLIER);
       roomPlayer.setJoined(false);
       roomPlayerRepository.save(roomPlayer);
    }

    @Transactional
    public RoomPlayer createRoomPlayer(Room room){

        RoomPlayer roomPlayer = RoomPlayer.builder()
                .room(room)
                .player(playerService.createPlayer(room.getId()))
                .build();
       return roomPlayerRepository.save(roomPlayer);

    }

    private List<PlayerProjection> getUsersNameByRoomId(Long roomId){
        return roomPlayerRepository.findUserNameByRoomId(roomId).orElseThrow(
                ()->new RuntimeException("Players is not found but code is valid"));
    }



    private Consumer<ProductDTO> preparingRequest(RoomPlayer roomPlayer){
        Player player = roomPlayer.getPlayer();
        return dto -> {
            dto.setPlayerId(player.getId());
            dto.setRoomId(roomPlayer.getRoom().getId());

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
                dto.setArtifactRand1Id(player.getSecondArtifactRandomCatalog().getId());
            }
        };
    }
}
