package com.github.shasnow.cabm4j.service;

import com.github.shasnow.cabm4j.entity.Character;
import com.github.shasnow.cabm4j.util.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CharacterService {
    private final Logger logger = LoggerFactory.getLogger(CharacterService.class);
    private final Map<String, Character> characters = new HashMap<>();
    private final String currentCharacterId = "SilverWolf"; // 默认角色ID

    public CharacterService() {
        // 初始化时可以加载默认角色
        logger.info("Initializing CharacterService");
        List<Path> tomlFiles = scanTomlFiles(Path.of(ConfigManager.getAppConfig().get("character_folder").toString()));
        for (Path tomlFile : tomlFiles) {
            Character character = Character.create(tomlFile);
            characters.put(character.getId(), character);
            logger.info("Loaded character: {}", character.getName());
        }
    }

    public List<Path> scanTomlFiles(Path directoryPath) {
        List<Path> tomlFiles = new ArrayList<>();

        // 检查路径是否存在且是目录
        if (!Files.exists(directoryPath) || !Files.isDirectory(directoryPath)) {
            throw new IllegalArgumentException("指定的路径不存在或不是目录: " + directoryPath);
        }

        // 使用 DirectoryStream 扫描当前目录
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directoryPath)) {
            for (Path path : stream) {
                // 只处理文件（不处理目录），并检查扩展名
                if (Files.isRegularFile(path) && path.toString().toLowerCase().endsWith(".toml")) {
                    tomlFiles.add(path.toAbsolutePath());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tomlFiles;
    }

    public Character loadCharacterById(String characterID) {
        logger.info("Loading character with id: {}", characterID);
        if (characters.containsKey(characterID)) return characters.get(characterID);
        else throw new IllegalArgumentException("Character with id: " + characterID + " not found");
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
