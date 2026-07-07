package com.example.bunker.controller;

import com.example.bunker.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/player")
public class PlayerController {

    private  final PlayerService playerService;

    @GetMapping("/hero/{roomId}")
    public ResponseEntity<?> getHero(@PathVariable Long roomId){

        return ResponseEntity.ok(playerService.addHero(roomId));
    }

    @GetMapping("/randomArtifacts/{roomId}")
    public ResponseEntity<?> getRandomArtifacts(@PathVariable Long roomId){

        return ResponseEntity.ok(playerService.findRandomArtifactCatalog(roomId));
    }

    @GetMapping("/heroArtifacts/{roomId}")
    public ResponseEntity<?> getHeroArtifacts(@PathVariable Long roomId){

        return  ResponseEntity.ok(playerService.addHeroArtifacts(roomId));
    }



    @PostMapping("/artifacts")
    public ResponseEntity<?> postTwoArtifacts( @RequestParam Long id1,
                                               @RequestParam Long id2,
                                               @RequestParam Long roomId){
        playerService.addTwoRandomArtifacts(id1,id2,roomId);
        return ResponseEntity.ok().build();
    }

}
