package com.example.bunker.controller;

import com.example.bunker.dto.Player.PlayerArtifactResponse;
import com.example.bunker.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/player")
public class PlayerController {

    private  final PlayerService playerService;

    @GetMapping("/hero")
    public ResponseEntity<?> getHero(@RequestParam Long roomId){

        return ResponseEntity.ok(playerService.addHero(roomId));
    }

    @GetMapping("/randomArtifacts")
    public ResponseEntity<?> getRandomArtifacts(@RequestParam Long roomId){

        return ResponseEntity.ok(playerService.findRandomArtifactCatalog(roomId));
    }

    @GetMapping("/heroArtifacts")
    public ResponseEntity<?> getHeroArtifacts(@RequestParam Long roomId){

        return  ResponseEntity.ok(playerService.addHeroArtifacts(roomId));
    }



    @PostMapping("/artifacts")
    public ResponseEntity<?> postTwoArtifacts( @RequestParam Long id1,
                                               @RequestParam Long id2,
                                               @RequestParam Long roomId){
        playerService.addTwoArtifacts(id1,id2,roomId);
        return ResponseEntity.ok().build();
    }

}
