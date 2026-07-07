package com.example.bunker.repository;

import com.example.bunker.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room,Long> {

    @Query("""
            Select r FROM Room r
            JOIN FETCH r.user u
            Where r.is = :roomId
            """)
    Optional<Room> findRoomAndUserBy(@Param("roomId") long id);

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

    Optional<String> findCodeToConnectById(Long  Id);



}
