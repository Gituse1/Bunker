package com.example.bunker.repository;

import com.example.bunker.model.ArtifactHeroCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ArtifactHeroCatalogRepository  extends JpaRepository<ArtifactHeroCatalog,Long> {


    @Query(value = "SELECT ahc FROM ArtifactHeroCatalog ahc ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<ArtifactHeroCatalog> findHeroArtifact();
}
