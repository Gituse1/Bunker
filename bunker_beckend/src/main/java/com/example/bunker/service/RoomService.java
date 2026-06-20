package com.example.bunker.service;

import com.example.bunker.dto.Room.AllRoomsResponse;
import com.example.bunker.dto.Room.RoomResponse;
import com.example.bunker.model.Room;
import com.example.bunker.repository.RoomRepository;
import com.example.bunker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class RoomService {

    private final SecureRandom secureRandom = new SecureRandom();

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    private final AuthService authService;
    private final RoomPlayerService roomPlayerService;

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";


    public RoomResponse createRoom() {

        Room room = new Room();
        String userName = authService.getCurrentUserName();

        room.setUser(userRepository.findByUsername(userName).orElseThrow(
                ()->new RuntimeException("Data authorisation was damaged")));

        room.setCreatedAt(LocalDateTime.now());
        room.setIfFinished(false);
        room=roomRepository.save(room);
        room.setCodeToConnect(generateToken());

        long roomPlayerId =roomPlayerService.createRoomPlayer(room).getId();

        return RoomResponse.builder()
                .id(roomPlayerId)
                .codeToConnect(room.getCodeToConnect())
                .build();
    }


    public List<AllRoomsResponse> getAllRooms() {

        String userName =authService.getCurrentUserName();

        List<Room> rooms =roomRepository.roomsByUserName(userName);
        if(rooms.isEmpty()) {
            throw new IllegalArgumentException("The user has not created any rooms yet.");
        }
        List<AllRoomsResponse> roomsRequests = new ArrayList<>();
        for (Room room : rooms) {
            AllRoomsResponse allRoomsResponse = AllRoomsResponse.builder()
                    .createdAt(room.getCreatedAt())
                    .isFinished(room.isIfFinished())
                    .id(room.getId())
                    .CodeToConnect(room.getCodeToConnect())
                    .build();
            roomsRequests.add(allRoomsResponse);
        }
        return roomsRequests;
    }



    public String generateToken() {
        String token;
        boolean isTokenUnique;
        do {
            StringBuilder sb = new StringBuilder(8);

            for (int i = 0; i < 8; i++) {
                sb.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
            }
            token= sb.toString();
            Optional<Room> room =roomRepository.findRoomByCodeToConnect(token);

            isTokenUnique= room.isEmpty();

        }while (!isTokenUnique);
        return token;
    }
}
