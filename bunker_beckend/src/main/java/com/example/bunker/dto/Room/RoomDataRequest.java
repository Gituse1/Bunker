package com.example.bunker.dto.Room;

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

    private Long id;
    private List<String> names;
    private List<Long> ids;

}
