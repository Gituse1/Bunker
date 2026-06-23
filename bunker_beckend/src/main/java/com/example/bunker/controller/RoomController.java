package com.example.bunker.controller;

import com.example.bunker.model.Room;
import com.example.bunker.service.GameService;
import com.example.bunker.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/room")
public class RoomController {

    private final RoomService roomService;
    private final GameService gameService;

    @PostMapping("/create")
    private ResponseEntity<?> createRoom() {
        return ResponseEntity.ok(roomService.createRoom());
    }

    @GetMapping("/all_rooms")
    private ResponseEntity<?> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }
    @GetMapping("/continueToGame")
    private ResponseEntity<?> continueToGame(@RequestBody Long roomId) {
        return ResponseEntity.ok(gameService.continueToGame(roomId));
    }

}
