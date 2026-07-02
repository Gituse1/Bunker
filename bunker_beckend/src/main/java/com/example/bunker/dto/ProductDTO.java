package com.example.bunker.dto;

import com.example.bunker.model.StatusInGame;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private String userName;
    private String voteSelectedName =null;
    private Long heroId;
    private Long playerId;
    private Long characterId;
    private Long artifactHeroId;
    private Long artifactRand1Id;
    private Long artifactRand2Id;
    private Long visibilityId;
    private Long effectId;
    private boolean isProtected;
    private Integer timeOfProtection =0;
    private boolean isStunned;
    private Integer timeOfStunned =0;
    private StatusInGame statusInGame =StatusInGame.PREPARATION_FOR_THE_GAME;

}
