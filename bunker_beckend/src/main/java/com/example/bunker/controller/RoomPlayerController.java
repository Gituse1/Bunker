package com.example.bunker.controller;

import com.example.bunker.service.RoomPlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/roomPlayer")
public class RoomPlayerController {

    private final RoomPlayerService roomPlayerService;

    @GetMapping("/continue_game")
    private ResponseEntity<?> continueGame( @RequestParam String codeToConnect) {
        return ResponseEntity.ok(roomPlayerService.connectToGame(codeToConnect));
    }


    @PutMapping("/left_game/{roomId}")
    private ResponseEntity<?> leftGame(@PathVariable Long roomId) {
        roomPlayerService.leaveGame(roomId);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/expulsion/{roomId}")
    private ResponseEntity<?> expulsion(@PathVariable Long roomId , @RequestBody String targetUserName) {
        roomPlayerService.playerExpulsion(roomId,targetUserName);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/results/{roomId}")
    private ResponseEntity<?> votingResults(@PathVariable Long roomId){
        roomPlayerService.votingResults(roomId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("next_move/{roomId}")
    private ResponseEntity<?> nextMove(@PathVariable Long roomId) {

        roomPlayerService.nextMove(roomId);
        return ResponseEntity.ok().build();
    }


}
