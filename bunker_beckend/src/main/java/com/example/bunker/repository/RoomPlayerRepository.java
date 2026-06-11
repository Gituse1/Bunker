package com.example.bunker.repository;

import com.example.bunker.model.RoomPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomPlayerRepository extends JpaRepository<RoomPlayer,Long> {

    Optional<RoomPlayer> findRoomPlayerByRoomId(Long roomId);
}
