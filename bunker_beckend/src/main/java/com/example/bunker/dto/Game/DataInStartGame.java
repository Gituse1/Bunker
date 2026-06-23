package com.example.bunker.dto.Game;

import com.example.bunker.model.VisibilityOfCharacteristic;
import com.example.bunker.model.game.GameData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataInStartGame {

    private GameData gameData;
    private VisibilityOfCharacteristic visibilityOfCharacteristic;
}
