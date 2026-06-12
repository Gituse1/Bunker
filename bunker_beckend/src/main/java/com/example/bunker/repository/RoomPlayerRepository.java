package com.example.bunker.repository;

import com.example.bunker.model.RoomPlayer;
import com.example.bunker.projection.PlayerProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomPlayerRepository extends JpaRepository<RoomPlayer,Long> {


    @Query("""
            Select u.name AS name, p.id AS id
            FROM RoomPlayer rp
            JOIN rp.player p
            JOIN p.user u
            WHERE rp.room.id =:roomId
            """)
    Optional<List<PlayerProjection>> findUserNameByRoomId(
            @Param("roomId") Long roomId
    );


    @Query("""
            Select rp
            FROM RoomPlayer rp
            JOIN rp.player p
            JOIN p.user u
            WHERE u.email = :userEmail AND
            rp.id = :roomPlayerId
            """)
    Optional<RoomPlayer> findByIdCurrentUser(
            @Param("roomPlayerID") Long roomPlayerId,
            @Param("userEmail") String email
    );

    @Query("""
        SELECT rp FROM roomPlayer rp
        JOIN player p ON rp.player.id = p.id
        JOIN users u ON p.user.id = u.id
        WHERE u.email = :userEmail AND rp.id = :roomPlayerId
    """)
    Optional<RoomPlayer> findPlayerByRoomPlayerId(
            @Param("roomPlayerId") Long roomPlayerId,
            @Param("userEmail") String userEmail
    );

}
