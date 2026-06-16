package com.example.bunker.controller;

import com.example.bunker.dto.Player.PlayerArtifactRequest;
import com.example.bunker.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/player")
public class PlayerController {

    private  final PlayerService playerService;

    //Описати поступове додавання характеристик до вже існуючого героя.

    @GetMapping("/artifacts")
    public ResponseEntity<?> getArtifacts(@RequestParam Long roomId){

        PlayerArtifactRequest playerArtifactRequest = PlayerArtifactRequest.builder()
                .artifactRandomCatalogs(playerService.findRandomArtifactCatalog(roomId))
                .artifactHeroCatalog(playerService.addHeroArtifacts(roomId))
                .build();

        return ResponseEntity.ok(playerArtifactRequest);
    }


    @PostMapping("/artifacts")
    public ResponseEntity<?> postTwoArtifacts( @RequestParam Long id1,
                                               @RequestParam Long id2,
                                               @RequestParam Long roomId){
        playerService.addTwoArtifacts(id1,id2,roomId);
        return ResponseEntity.ok().build();
    }

}
