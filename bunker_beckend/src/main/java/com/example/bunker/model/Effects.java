package com.example.bunker.model;

import com.example.bunker.dto.ProductDTO;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.function.BiConsumer;
import java.util.function.Function;

@AllArgsConstructor
@RequiredArgsConstructor
public enum Effects {

    STUN(
            ProductDTO::isStunned,
            ProductDTO::setStunned
    ),
    PROTECT(
            ProductDTO::isProtected,
            ProductDTO::setProtected
    );

    private final Function<ProductDTO, Boolean> valueExtractor;
    private final BiConsumer<ProductDTO, Boolean> valueSetter;

    public boolean isUnderEffect(ProductDTO dto) {
        return valueExtractor.apply(dto);
    }
    public ProductDTO setEffect(ProductDTO dto) {
         valueSetter.accept(dto,true);
         return dto;
    }

}

