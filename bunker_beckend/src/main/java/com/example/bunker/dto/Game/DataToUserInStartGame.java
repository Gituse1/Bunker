package com.example.bunker.dto.Game;

import com.example.bunker.model.game.GameData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DataToUserInStartGame {
    List<GameData> otherUserGameDataList;
    GameData userGameData;
}
