package com.example.bunker.repository;

import com.example.bunker.model.Hero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface HeroRepository extends JpaRepository<Hero,Long> {

    @Query(value = "SELECT h FROM Hero h ORDER BY RANDOM() LIMIT 1")
    Optional<Hero> findHero();
}
