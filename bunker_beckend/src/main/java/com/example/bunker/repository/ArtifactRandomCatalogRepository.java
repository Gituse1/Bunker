package com.example.bunker.repository;

import com.example.bunker.model.ArtifactRandomCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ArtifactRandomCatalogRepository  extends JpaRepository<ArtifactRandomCatalog,Integer> {

    @Query(value = "SELECT * FROM artifact_random_catalog ORDER BY RAND() LIMIT 4", nativeQuery = true)
    Optional<List<ArtifactRandomCatalog>> findRandomArtifact();
}
