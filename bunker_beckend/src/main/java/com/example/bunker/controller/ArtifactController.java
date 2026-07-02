package com.example.bunker.controller;

import com.example.bunker.dto.Artifact.DataToUsingArtifactRequest;
import com.example.bunker.model.Effects;
import com.example.bunker.service.ArtifactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/randomArtifact/using")
public class ArtifactController {

    private final ArtifactService artifactService;

    @PutMapping("/purification/{roomId}")
    public ResponseEntity<?> usePurification(@PathVariable Long roomId,@RequestBody DataToUsingArtifactRequest dataToUsingArtifactRequest){

        artifactService.usePurification(dataToUsingArtifactRequest.getArtifactId(),
                roomId,
                dataToUsingArtifactRequest.getTargetPlayerId(),
                dataToUsingArtifactRequest.getCharacteristicToChange());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/protection/{roomId}")
    public ResponseEntity<?> useProtection(@RequestBody Long artifactId,
                                           @PathVariable Long roomId){

        artifactService.underEffect(artifactId,roomId, Effects.PROTECT);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/espionage/{roomId}")
    public ResponseEntity<?> useEspionage(@RequestParam Long roomId,@RequestBody DataToUsingArtifactRequest dataToUsingArtifactRequest){

        return ResponseEntity.ok(artifactService.useEspionage(
                dataToUsingArtifactRequest.getArtifactId(),
                roomId,
                dataToUsingArtifactRequest.getTargetPlayerId(),
                dataToUsingArtifactRequest.getCharacteristicToChange()));
    }

    @PutMapping("/stun")
    public ResponseEntity<?> useStun(@RequestBody Long artifactId,
                                     @RequestBody Long roomId){

        return ResponseEntity.ok(artifactService.underEffect(roomId,artifactId, Effects.STUN));
    }

    @PutMapping("/stealing/{roomId}")
    public ResponseEntity<?> useStealing(@PathVariable Long roomId,@RequestBody DataToUsingArtifactRequest dataToUsingArtifactRequest){

        return ResponseEntity.ok(artifactService.useStealing(
                dataToUsingArtifactRequest.getArtifactId(),
                roomId,
                dataToUsingArtifactRequest.getTargetPlayerId(),
                dataToUsingArtifactRequest.getCharacteristicToChange())
        );
    }

    @PutMapping("/curse/{roomId}")
    private ResponseEntity<?> useCurse(@PathVariable Long roomId,@RequestBody DataToUsingArtifactRequest dataToUsingArtifactRequest){

        artifactService.useCurse(
                dataToUsingArtifactRequest.getArtifactId(),
                roomId,
                dataToUsingArtifactRequest.getTargetPlayerId(),
                dataToUsingArtifactRequest.getCharacteristicToChange());
        return ResponseEntity.ok().build();
    }
}
