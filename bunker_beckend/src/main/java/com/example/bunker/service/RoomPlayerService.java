package com.example.bunker.service;

import com.example.bunker.dto.Room.RoomDataRequest;
import com.example.bunker.model.Room;
import com.example.bunker.model.RoomPlayer;
import com.example.bunker.projection.PlayerProjection;
import com.example.bunker.repository.RoomPlayerRepository;
import com.example.bunker.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomPlayerService {

    private final RoomPlayerRepository roomPlayerRepository;
    private final RoomRepository roomRepository;
    private final AuthService authService;


    public RoomDataRequest connectToGame(String codeToConnect) {
        Room room =roomRepository.findRoomByCode(codeToConnect).orElseThrow(
                ()->new RuntimeException("Code is not valid or something went wrong"));

        String userName =authService.getCurrentUserName();
        Long roomPlayerId;

        RoomPlayer roomPlayer =roomPlayerRepository.findPlayerByRoomPlayerId(room.getId(),userName)
                .orElseGet(() ->createRoomPlayer(room));

        roomPlayerId=roomPlayer.getId();

        List<PlayerProjection> projections = getUserNameByRoomId(room.getId());

        List<String> names = new ArrayList<>();

        List<Long> idUser = new ArrayList<>();

        for(PlayerProjection projection:projections){
            names.add(projection.getName());
            idUser.add(projection.getPlayerId());
        }

        return  RoomDataRequest.builder()
                .names(names)
                .id(roomPlayerId)
                .ids(idUser)
                .build();
    }

    public void leaveGame(Long id) {
        String name =authService.getCurrentUserName();
       RoomPlayer roomPlayer= roomPlayerRepository.findByIdCurrentUser(id,name).orElseThrow(
               ()->new RuntimeException("Player is not exist"));
       roomPlayer.setJoined(false);
       roomPlayerRepository.save(roomPlayer);
    }

    public RoomPlayer createRoomPlayer(Room room){

        RoomPlayer roomPlayer = RoomPlayer.builder()
                .room(room)
                .build();
       return roomPlayerRepository.save(roomPlayer);

    }

    private List<PlayerProjection> getUserNameByRoomId(Long roomId){
        return roomPlayerRepository.findUserNameByRoomId(roomId).orElseThrow(
                ()->new RuntimeException("Players is not found but code is valid"));
    }

}
