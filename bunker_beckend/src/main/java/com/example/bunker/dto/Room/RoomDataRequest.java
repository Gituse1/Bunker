package com.example.bunker.dto.Room;

import com.example.bunker.model.Player;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomDataRequest {

    private Long roomId;
    private Player player;
    private List<String> names;
    private List<Long> ids;

}
