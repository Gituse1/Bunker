package com.example.bunker.dto.Player;

import com.example.bunker.model.ArtifactHeroCatalog;
import com.example.bunker.model.ArtifactRandomCatalog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PlayerArtifactRequest {

    ArtifactHeroCatalog artifactHeroCatalog;
    List<ArtifactRandomCatalog> artifactRandomCatalogs;

}
