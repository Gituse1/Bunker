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
            Select u.username , p.id
            FROM RoomPlayer rp
            JOIN rp.player p
            JOIN p.user u
            WHERE rp.room.id =:roomId
            """)
    List<PlayerProjection> findUserNameByRoomId(
            @Param("roomId") Long roomId
    );


    @Query("""
            Select rp
            FROM RoomPlayer rp
            JOIN FETCH rp.player p
            JOIN p.user u
            WHERE u.username = :userName AND
            rp.id = :roomPlayerId
            """)
    Optional<RoomPlayer> findByIdCurrentRoomPlayer(
            @Param("roomPlayerId") Long roomPlayerId,
            @Param("userName") String name
    );

    @Query("""
        SELECT rp FROM RoomPlayer rp
        JOIN rp.player p
        JOIN p.user u
        WHERE u.username = :userName AND rp.id = :roomPlayerId
    """)
    Optional<RoomPlayer> findPlayerByRoomPlayerId(
            @Param("roomPlayerId") Long roomPlayerId,
            @Param("userName") String userName
    );

}
