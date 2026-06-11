package com.example.bunker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="room_player")
public class RoomPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne()
    @JoinColumn(name ="room_id")
    private Room room;

    @ManyToOne()
    @JoinColumn(name ="player_id")
    private Player player;

    @Column(name ="joined_at")
    private LocalDateTime joinedAt=LocalDateTime.now();
}
