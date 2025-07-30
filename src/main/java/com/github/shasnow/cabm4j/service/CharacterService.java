package com.github.shasnow.cabm4j.service;

import com.github.shasnow.cabm4j.entity.Character;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CharacterService {
    private final Logger logger = LoggerFactory.getLogger(CharacterService.class);
    private final Map<String, Character> characters = new HashMap<>();
    private final String currentCharacterId = "SilverWolf"; // 默认角色ID

    public CharacterService() {
        // 初始化时可以加载默认角色
        logger.info("Initializing CharacterService with default character");
        getCharacterById(currentCharacterId);
    }

    public Character loadCharacterById(String characterID) {
        logger.info("Loading character with id: {}", characterID);
        if (characters.containsKey(characterID)) return characters.get(characterID);
        String fullClassName = "com.github.shasnow.cabm4j.entity." + characterID;
        try {
            Class<?> characterClass = Class.forName(fullClassName);
            Object characterInstance = characterClass.getDeclaredConstructor().newInstance();
            assert characterInstance instanceof Character : "Loaded class is not a Character instance";
            characters.put(characterID, (Character) characterInstance);
            return ((Character) characterInstance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load character config for " + characterID, e);
        }
    }

    public Character getCharacterById(String characterID) {
        if (characterID == null || characterID.isEmpty()) {
            characterID = "SilverWolf";
        }
        return loadCharacterById(characterID);
    }

    public Map<String, Character> getAllCharacters() {
        return characters;
    }

    public Character getCurrentCharacter() {
        return getCharacterById(currentCharacterId);
    }
}
