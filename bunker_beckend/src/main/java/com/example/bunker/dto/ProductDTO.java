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
    private Long roomId;
    private Long heroId;
    private Long characterId;
    private Long artifactHeroId;
    private Long artifactRand1Id;
    private Long artifactRand2Id;
}
