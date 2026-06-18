package com.example.bunker.repository;


import com.example.bunker.model.CharacteristicPlayer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacteristicRepository extends JpaRepository<CharacteristicPlayer,Long> {

}
