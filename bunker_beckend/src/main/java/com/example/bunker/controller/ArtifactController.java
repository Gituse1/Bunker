package com.example.bunker.controller;

import com.example.bunker.model.Effects;
import com.example.bunker.model.characteristic.Characteristic;
import com.example.bunker.service.ArtifactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("app/randomArtifact/using")
public class ArtifactController {

    private final ArtifactService artifactService;

    @PutMapping("/purification")
    public ResponseEntity<?> usePurification(@RequestBody Long artifactId,
                                             @RequestBody Long roomId,
                                             @RequestBody Long playerId,
                                             @RequestBody Characteristic characteristicToChange){

        artifactService.usePurification(artifactId,roomId,playerId,characteristicToChange);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/protection")
    public ResponseEntity<?> useProtection(@RequestBody Long artifactId,
                                           @RequestBody Long roomId){

        artifactService.underEffect(artifactId,roomId, Effects.PROTECT);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/espionage")
    public ResponseEntity<?> useEspionage(@RequestBody Long artifactId,
                                          @RequestBody Long roomId,
                                          @RequestBody Long targetPlayerId,
                                          @RequestBody Characteristic characteristic){

        return ResponseEntity.ok(artifactService.useEspionage(artifactId,roomId,targetPlayerId,characteristic));
    }

    @PutMapping("/stun")
    public ResponseEntity<?> useStun(@RequestBody Long artifactId,
                                     @RequestBody Long roomId){

        return ResponseEntity.ok(artifactService.underEffect(roomId,artifactId, Effects.STUN));
    }

    @PutMapping("/stealing")
    public ResponseEntity<?> useStealing(){

        return ResponseEntity.ok().build();
    }

    @PutMapping("/curse")
    private ResponseEntity<?> useCurse(@RequestBody Long artifactId,
                                       @RequestBody Long roomId,
                                       @RequestBody Long targetPlayerId,
                                       @RequestBody Characteristic characteristicToChange){

        artifactService.useCurse(artifactId,roomId,targetPlayerId,characteristicToChange);
        return ResponseEntity.ok().build();
    }
}
