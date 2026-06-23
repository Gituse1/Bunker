package com.example.bunker.model.characteristic;


import com.example.bunker.dto.ProductDTO;
import com.example.bunker.dto.Characteristic.CharacteristicSourceDto;
import com.example.bunker.model.*;
import com.example.bunker.projection.CharacteristicSource;


import java.util.function.BiConsumer;
import java.util.function.Function;


public enum Characteristic {
    PROFESSION(
            VisibilityOfCharacteristic::isProfessionIsVisible,
            source ->((Hero)source).getProfession().toString(),
            (source,value) ->((Hero)source).setProfession(Profession.valueOf(value)),
            ProductDTO::setHeroId
    ),
    STATE_OF_HEALTH(
            VisibilityOfCharacteristic::isStateOfHealthIsVisible,
            source ->((CharacteristicPlayer)source).getStateOfHealth().toString(),
            (source,value) ->((CharacteristicPlayer)source).setStateOfHealth(StateOfHealth.valueOf(value)),
            ProductDTO::setCharacterId
    ),
    FIGURE(
            VisibilityOfCharacteristic::isFigureIsVisible,
            source -> ((CharacteristicPlayer) source).getFigure().toString(),
            (source,value) ->((CharacteristicPlayer) source).setFigure(Figure.valueOf(value)),
            ProductDTO::setCharacterId
    ),
    RASE(
            VisibilityOfCharacteristic::isRaseIsVisible,
            source ->((Hero)source).getRace().toString(),
            (source,value)->((Hero)source).setRace(Race.valueOf(value)),
            ProductDTO::setHeroId
    ),
    PHYSICAL_CONDITION(
            VisibilityOfCharacteristic::isPhysicalConditionIsVisible,
            source -> ((CharacteristicPlayer)source).getPhysicalCondition().toString(),
            (source,value)->((CharacteristicPlayer)source).setPhysicalCondition(PhysicalCondition.valueOf(value)),
            ProductDTO::setCharacterId
    ),
    PSYCHOLOGICAL_STATE(
            VisibilityOfCharacteristic::isPsyhologicalStateIsVisible,
            source -> ((CharacteristicPlayer)source).getPsyhologicalState().toString(),
            (source,value)-> ((CharacteristicPlayer)source).setPsyhologicalState(PsychologicalState.valueOf(value)),
            ProductDTO::setCharacterId
    ),
    SECRET(
            VisibilityOfCharacteristic::isSecretsIsVisible,
            source -> ((CharacteristicPlayer)source).getSecret().toString(),
            (source,value)->((CharacteristicPlayer)source).setSecret(Secret.valueOf(value)),
            ProductDTO::setCharacterId
    ),
    GROWN(
            VisibilityOfCharacteristic::isGrownIsVisible,
            source -> ((CharacteristicPlayer)source).getGrown().toString(),
            (source, value) -> ((CharacteristicPlayer) source).setGrown(Double.parseDouble(value)),
            ProductDTO::setCharacterId
    ),
    HOBBY(
            VisibilityOfCharacteristic::isHobbyIsVisible,
            source -> ((Hero)source).getHobby(),
            (source,value) ->((Hero)source).setHobby(String.valueOf(Hobby.valueOf(value))),
            ProductDTO::setHeroId
    ),
    INVENTORY1(
            VisibilityOfCharacteristic::isArtefactHeroIsVisible,
            source -> ((ArtifactHeroCatalog)source).getName(),
            (source,value)->((ArtifactHeroCatalog)source).setName(value),
            ProductDTO::setArtifactHeroId
    ),
    INVENTORY2(
            VisibilityOfCharacteristic::isArtefactRandom1IsVisible,
            source -> ((ArtifactRandomCatalog)source).getName(),
            (source,value)->((ArtifactRandomCatalog)source).setName(value),
            ProductDTO::setArtifactRand1Id
    ),
    INVENTORY3(
            VisibilityOfCharacteristic::isArtefactRandom2IsVisible,
            source -> ((ArtifactRandomCatalog)source).getName(),
            (source,value)->((ArtifactRandomCatalog)source).setName(value),
            ProductDTO::setArtifactRand2Id
    );


    private final Function<VisibilityOfCharacteristic, Boolean> visibilityCheck;
    private final Function<CharacteristicSource, String> valueExtractor;
    private final BiConsumer<CharacteristicSource, String> valueSetter;
    private final BiConsumer<ProductDTO,Long> productSetter;

    Characteristic(
            Function<VisibilityOfCharacteristic, Boolean> visibilityCheck,
            Function<CharacteristicSource, String> stringValueExtractor,
            BiConsumer<CharacteristicSource, String> valueSetter,
            BiConsumer<ProductDTO,Long> productSetter
    ){
        this.visibilityCheck = visibilityCheck;
        this.valueExtractor = stringValueExtractor;
        this.valueSetter = valueSetter;
        this.productSetter = productSetter;
    }


    public boolean isVisible(VisibilityOfCharacteristic visibility) {
        return visibilityCheck.apply(visibility);
    }

    public String extractValue(CharacteristicSource source) {
        return valueExtractor.apply(source);
    }

    public CharacteristicSourceDto swapCharacteristic(CharacteristicSource firstUserSource, CharacteristicSource targetUserSource) {
        String nameOf =valueExtractor.apply(firstUserSource);
        valueSetter.accept(firstUserSource, valueExtractor.apply(targetUserSource));
        valueSetter.accept(targetUserSource, nameOf);

        return CharacteristicSourceDto.builder()
                .userCharacteristicSource(firstUserSource)
                .targetCharacteristicSource(targetUserSource)
                .build();
    }




}
