package com.example.bunker.controller;

import com.example.bunker.service.RoomPlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/app/roomPlayer")
public class RoomPlayerController {

    private final RoomPlayerService roomPlayerService;

    @GetMapping("/continue_game")
    private ResponseEntity<?> continueGame( @RequestParam String codeToConnect) {
        return ResponseEntity.ok(roomPlayerService.connectToGame(codeToConnect));
    }


    @PutMapping("/left_game")
    private ResponseEntity<?> leftGame(@ RequestParam Long roomId) {
        roomPlayerService.leaveGame(roomId);
        return ResponseEntity.ok().build();
    }
}
