package com.example.bunker.repository;


import com.example.bunker.model.CharacteristicPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CharacteristicRepository extends JpaRepository<CharacteristicPlayer,Long> {

    @Query(value = "SELECT cp FROM characteristic cp ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<CharacteristicPlayer> findRandomArtifact();
}
