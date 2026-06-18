package com.example.bunker.repository;

import com.example.bunker.model.CharacteristicPlayer;
import com.example.bunker.model.Player;
import com.example.bunker.model.StatusInGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface PlayerRepository extends JpaRepository<Player, Long> {

    @Query("""
            SELECT p FROM Player p
            WHERE p.user.id = :userId AND
            p.status = :status
            """)

    Optional<Player> findByStatusAndUser(
            @Param("userId") Long userId,
            @Param("status") StatusInGame status
            );

    @Modifying
    @Transactional
    @Query("""
            UPDATE Player p SET p.character = :character
            WHERE p.id = :id
            """)
    int updateCharacter(
            @Param("id") Long playerId,
            @Param("character") CharacteristicPlayer character
    );
}
