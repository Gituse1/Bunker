package com.example.bunker.controller;

import com.example.bunker.service.RoomPlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/app/roomPlayer")
public class RoomPlayerController {

    private final RoomPlayerService roomPlayerService;

    @GetMapping("/continue_game")
    private ResponseEntity<?> continueGame(String codeToConnect) {
        return ResponseEntity.ok(roomPlayerService.connectToGame(codeToConnect));
    }


    @PutMapping("/left_game")
    private ResponseEntity<?> leftGame(Long id) {
        roomPlayerService.leaveGame(id);
        return ResponseEntity.ok().build();
    }
}
