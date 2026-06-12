package com.example.bunker.controller;

import com.example.bunker.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/player")
public class PlayerController {

    private  final PlayerService playerService;

    @PostMapping("/create")
    public ResponseEntity<?> createPlayer(Long roomId){

        return ResponseEntity.ok(playerService.createPlayer(roomId));
    }

    @GetMapping("/getArtifacts")
    public ResponseEntity<?> getArtifacts(Long roomId){
        return ResponseEntity.ok(playerService.findRandomArtifactCatalog(roomId));
    }


    @PostMapping("/postArtifacts")
    public ResponseEntity<?> postTwoArtifacts( Long id1, Long id2,Long roomId){
        playerService.addTwoArtifacts(id1,id2,roomId);
        return ResponseEntity.ok().build();
    }

}
