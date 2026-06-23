package com.example.bunker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long heroId;
    private Long playerId;
    private Long characterId;
    private Long artifactHeroId;
    private Long artifactRand1Id;
    private Long artifactRand2Id;
    private Long visibilityId;
    private boolean isProtected;
    private boolean isStunned;

}
