package com.example.bunker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name="finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "code_to_connect")
    private String codeToConnect;

    @Column(name = "max_quantity_players")
    private int maxQuantityPlayers =10;

    @Column(name = "available_quantity_players")
    private int availableQuantityPlayers =1;

    @Column(name = "if_finished")
    private boolean ifFinished;

}
