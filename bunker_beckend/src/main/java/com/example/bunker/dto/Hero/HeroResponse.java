package com.example.bunker.dto.Hero;

import com.example.bunker.model.Hero;
import com.example.bunker.model.Profession;
import com.example.bunker.model.Rase;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HeroResponse {

    public HeroResponse(Hero hero) {
        this.name = hero.getName();
        this.skills = hero.getSkills();
        this.profession = hero.getProfession();
        this.rase = hero.getRase();
        this.hobby = hero.getHobby();
    }
    private String name;
    private Profession profession;
    private Rase rase;

    private String hobby;

    private String[] skills;
}
