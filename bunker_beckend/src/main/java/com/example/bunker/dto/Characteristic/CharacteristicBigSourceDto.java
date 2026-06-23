package com.example.bunker.dto.Characteristic;


import com.example.bunker.dto.ProductDTO;

import java.util.function.BiConsumer;

public class CharacteristicBigSourceDto extends CharacteristicSourceDto {

    BiConsumer<ProductDTO,Long> userProductSetter;
    BiConsumer<ProductDTO,Long> targetProductSetter;

}
