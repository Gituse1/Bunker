package com.example.bunker.repository;

import com.example.bunker.model.ArtifactHeroCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ArtifactHeroCatalogRepository  extends JpaRepository<ArtifactHeroCatalog,Long> {


    @Query(value = "SELECT * FROM artifact_hero_catalog ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<ArtifactHeroCatalog> findHeroArtifact();
}
