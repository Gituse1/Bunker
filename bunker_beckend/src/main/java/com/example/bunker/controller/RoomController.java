package com.example.bunker.controller;

import com.example.bunker.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/room")
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/create")
    private ResponseEntity<?> createRoom() {
        return ResponseEntity.ok(roomService.createRoom());
    }

    @GetMapping("/all_rooms")
    private ResponseEntity<?> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/continue_game")
    private ResponseEntity<?> continueGame( String codeToConnect) {
        return ResponseEntity.ok(roomService.continueGame(codeToConnect));
    }
}
