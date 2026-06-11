package com.example.bunker.repository;

import com.example.bunker.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room,Long> {
    Optional<Room> findById(long id);

    @Query("""
            SELECT * FROM Room r
            WHERE r.user_id =:userId
            """)

    Optional<List<Room>> findAllUsersRoom(
            @Param("userId") long id
    ) ;

    Optional<List<Room>> roomsByUserName(String Name);

    Optional<Room> findRoomByCode(String code);

}
