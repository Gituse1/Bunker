package com.example.bunker.model;

import com.example.bunker.dto.ProductDTO;

import java.util.function.BiConsumer;
import java.util.function.Function;

public enum Effects {

    STUN(
            ProductDTO::isStunned,
            ProductDTO::setStunned,
            ProductDTO::setTimeOfStunned,
            1,
            Effect::setStunned,
            Effect::setTimeOfStun

    ),
    PROTECT(
            ProductDTO::isProtected,
            ProductDTO::setProtected,
            ProductDTO::setTimeOfProtection,
            1,
            Effect::setProtected,
            Effect::setTimeOfProtection
    );

    private final Function<ProductDTO, Boolean> valueExtractor;
    private final BiConsumer<ProductDTO, Boolean> valueSetter;
    private final BiConsumer<ProductDTO, Integer> timeOfEffectSetter;
    private final  Integer timeOfEffect;
    private final BiConsumer<Effect, Boolean> valueSetterEffect;
    private final BiConsumer<Effect, Integer> timeOfEffectSetterEffect;

    Effects(Function<ProductDTO, Boolean> valueExtractor,
            BiConsumer<ProductDTO, Boolean> valueSetter,
            BiConsumer<ProductDTO, Integer> timeOfEffectSetter,
            Integer timeOfEffect,
            BiConsumer<Effect, Boolean> valueSetterEffect,
            BiConsumer<Effect, Integer> timeOfEffectSetterEffect){
        this.valueExtractor = valueExtractor;
        this.valueSetter = valueSetter;
        this.timeOfEffectSetterEffect = timeOfEffectSetterEffect;
        this.timeOfEffect = timeOfEffect;
        this.valueSetterEffect = valueSetterEffect;
        this.timeOfEffectSetter = timeOfEffectSetter;
    }

    public boolean isUnderEffect(ProductDTO dto) {
        return valueExtractor.apply(dto);
    }

    public ProductDTO setEffectToDto(ProductDTO dto) {
         valueSetter.accept(dto,true);
         timeOfEffectSetter.accept(dto,timeOfEffect);
         return dto;
    }
    public Effect setEffectToEffects(Effect effects) {
        valueSetterEffect.accept(effects,true);
        timeOfEffectSetterEffect.accept(effects,timeOfEffect);
        return effects;
    }

}

