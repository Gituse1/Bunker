package com.example.bunker.service;

import com.example.bunker.dto.Room.AllRoomsRequest;
import com.example.bunker.dto.Room.RoomDataRequest;
import com.example.bunker.dto.Room.RoomRequest;
import com.example.bunker.model.Room;
import com.example.bunker.projection.PlayerProjection;
import com.example.bunker.repository.RoomRepository;
import com.example.bunker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class RoomService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    private final AuthService authService;
    private final RoomPlayerService roomPlayerService;

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";


    public RoomRequest createRoom() {

        Room room = new Room();

        room.setUser(userRepository.findByUsername(authService.getCurrentUserName()).orElseThrow(
                ()->new RuntimeException("Data authorisation was damaged")));

        room.setCreatedAt(LocalDateTime.now());
        room.setIfFinished(false);
        room=roomRepository.save(room);

        long roomPlayerId =roomPlayerService.createRoomPlayer(room).getId();

        return RoomRequest.builder()
                .id(roomPlayerId)
                .codeToConnect(generateToken())
                .build();
    }


    public List<AllRoomsRequest> getAllRooms() {

        String userName =authService.getCurrentUserName();

        List<Room> rooms =roomRepository.roomsByUserName(userName).orElseThrow(
                () -> new RuntimeException("The user has not created any rooms yet.")
        );
        List<AllRoomsRequest> roomsRequests = new ArrayList<>();
        for (Room room : rooms) {
            AllRoomsRequest allRoomsRequest = AllRoomsRequest.builder()
                    .createdAt(room.getCreatedAt())
                    .isFinished(room.isIfFinished())
                    .id(room.getId())
                    .CodeToConnect(room.getCodeToConnect())
                    .build();
            roomsRequests.add(allRoomsRequest);
        }
        return roomsRequests;
    }



    public String generateToken() {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder sb = new StringBuilder(8);

        for (int i = 0; i < 8; i++) {
            sb.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        }

        return sb.toString();
    }
}
