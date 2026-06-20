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
            SELECT r FROM Room r
            WHERE r.user.id =:userId
            """)

    List<Room> findAllUsersRoom(
            @Param("userId") long id
    ) ;

    @Query("""
            SELECT r FROM Room r
            WHERE r.user.username = :userName
            """)
    List<Room> roomsByUserName( @Param("userName") String name);

    Optional<Room> findRoomByCodeToConnect(String codeToConnect);

    Optional<String> findCodeToConnectByRoomId(Long  roomId);

}
