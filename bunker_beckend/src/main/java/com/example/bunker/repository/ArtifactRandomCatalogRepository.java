package com.example.bunker.repository;

import com.example.bunker.model.ArtifactRandomCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Optional;

public interface ArtifactRandomCatalogRepository  extends JpaRepository<ArtifactRandomCatalog,Integer> {

    @Query(value = "SELECT * FROM artifact_random_catalog ORDER BY RAND() LIMIT 4", nativeQuery = true)
    Optional<List<ArtifactRandomCatalog>> findRandomArtifact();

    @Query("""
            Select * FROM artifact_random_catalog arc
            WHERE arc.id = :id1 OR arc.id = :id2
            """)
    Optional<List<ArtifactRandomCatalog>> findByIds(
            @Param("id1") Long id1,
            @Param("id2") Long id2);
}
