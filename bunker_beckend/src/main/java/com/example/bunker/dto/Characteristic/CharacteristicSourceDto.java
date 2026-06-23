package com.example.bunker.dto.Characteristic;

import com.example.bunker.projection.CharacteristicSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CharacteristicSourceDto{
        public CharacteristicSource userCharacteristicSource;
        public CharacteristicSource targetCharacteristicSource;

}
