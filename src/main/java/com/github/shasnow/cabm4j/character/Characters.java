package com.github.shasnow.cabm4j.character;

import java.util.HashMap;
import java.util.Map;

public class Characters {
    private static final Map<String,Character> characterConfigs=new HashMap<>();
    private static final String defaultCharacterID="Silver_Wolf";

    public static Map<String,Object> loadCharacterConfig(String characterID) {
        if (characterConfigs.containsKey(characterID)) return characterConfigs.get(characterID).getConfig();
        String fullClassName = "com.github.shasnow.cabm4j.character." + characterID;
        try {
            Class<?> characterClass = Class.forName(fullClassName);
            Object characterInstance = characterClass.getDeclaredConstructor().newInstance();
            assert characterInstance instanceof Character : "Loaded class is not a Character instance";
            characterConfigs.put(characterID, (Character) characterInstance);
            return ((Character) characterInstance).getConfig();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load character config for " + characterID, e);
        }
    }

    public static Map<String, Object> getCharacterConfig(String characterID) {
        if (characterID == null || characterID.isEmpty()) {
            characterID = defaultCharacterID;
        }
        return loadCharacterConfig(characterID);
    }
}
