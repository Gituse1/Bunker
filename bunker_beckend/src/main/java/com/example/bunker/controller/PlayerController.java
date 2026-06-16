package com.example.bunker.controller;

import com.example.bunker.dto.Player.PlayerArtifactRequest;
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

    //Описати поступове додавання характеристик до вже існуючого героя.

    @GetMapping("/artifacts")
    public ResponseEntity<?> getArtifacts(Long roomId){

        PlayerArtifactRequest playerArtifactRequest = PlayerArtifactRequest.builder()
                .artifactRandomCatalogs(playerService.findRandomArtifactCatalog(roomId))
                .artifactHeroCatalog(playerService.addHeroArtifacts(roomId))
                .build();

        return ResponseEntity.ok(playerArtifactRequest);
    }


    @PostMapping("/artifacts")
    public ResponseEntity<?> postTwoArtifacts( Long id1, Long id2,Long roomId){
        playerService.addTwoArtifacts(id1,id2,roomId);
        return ResponseEntity.ok().build();
    }

}
