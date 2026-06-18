package com.example.bunker.controller;

import com.example.bunker.model.ActionTypeArtifact;
import com.example.bunker.model.characteristic.Characteristic;
import com.example.bunker.service.ArtifactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/randomArtifact/using")
public class ArtifactController {

    private final ArtifactService artifactService;

    @PutMapping("/purification")
    public ResponseEntity<?> usePurification(@RequestBody Long artifactId,
                                             @RequestBody Long roomId,
                                             @RequestBody Long playerId,
                                             @RequestBody Characteristic characteristic){

        artifactService.usePurification(artifactId,roomId,playerId,characteristic);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/protection")
    public ResponseEntity<?> useProtection(@RequestBody Long artifactId,
                                           @RequestBody Long roomId){

        artifactService.underEffect(artifactId,roomId, ActionTypeArtifact.PROTECTION);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/espionage")
    public ResponseEntity<?> useEspionage(){

        return ResponseEntity.ok().build();
    }

    @PutMapping("/stun")
    public ResponseEntity<?> useStun(@RequestBody Long artifactId,
                                     @RequestBody Long roomId){
        artifactService.underEffect(roomId,artifactId, ActionTypeArtifact.STUN);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/stealing")
    public ResponseEntity<?> useStealing(){

        return ResponseEntity.ok().build();
    }

    @PutMapping("/curse")
    private ResponseEntity<?> useCurse(){

        return ResponseEntity.ok().build();
    }
}
