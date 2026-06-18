package com.example.bunker.dto.Room;

import com.example.bunker.model.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AllRoomsResponse {

    private long id;
    private LocalDateTime createdAt;
    private String CodeToConnect;
    private Room room;
    private boolean isFinished;



}
