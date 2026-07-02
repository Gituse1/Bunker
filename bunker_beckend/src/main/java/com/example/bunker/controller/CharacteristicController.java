package com.example.bunker.controller;

import com.example.bunker.service.CharacteristicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/characteristic")
public class CharacteristicController {

    private final CharacteristicService characteristicService;

    @GetMapping("characteristic/{roomId}")
    public ResponseEntity<?> getCharacteristic(@PathVariable Long roomId){

        return ResponseEntity.ok(characteristicService.createCharacteristic(roomId));
    }
}
