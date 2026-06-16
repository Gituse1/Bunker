package com.example.bunker.repository;

import com.example.bunker.model.Player;
import com.example.bunker.model.StatusInGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface PlayerRepository extends JpaRepository<Player, Integer> {

    @Query("""
            SELECT * FROM Player p
            WHERE p.user = :userId AND
            p.status = :status
            """)

    Optional<Player> findByStatusAndUser(
            @Param("userId") Long userId,
            @Param("status") StatusInGame status
            );
}
