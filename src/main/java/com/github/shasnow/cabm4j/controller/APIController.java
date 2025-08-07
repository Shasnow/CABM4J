package com.github.shasnow.cabm4j.controller;

import com.alibaba.fastjson2.JSONObject;
import com.github.shasnow.cabm4j.entity.Character;
import com.github.shasnow.cabm4j.service.CharacterService;
import com.github.shasnow.cabm4j.service.ChatService;
import com.github.shasnow.cabm4j.service.ImageService;
import com.github.shasnow.cabm4j.service.OptionService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api")
public class APIController {
    private static final Logger logger = LoggerFactory.getLogger(APIController.class);
    @Resource
    ImageService imageService;
    @Resource
    CharacterService characterService;
    @Resource
    ChatService chatService;
    @Resource
    OptionService optionService;


    @GetMapping(value = "/chat/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<String> chatStream(@RequestParam String message){
        logger.info("Chat stream request received with messages: {}", message);
        return chatService.chatCompletionsStream(message);
    }
    @GetMapping("/background")
    @ResponseBody
    public String getCurrentBackground() {
        String background = imageService.getCurrentBackground();
        if (background == null) {
            Map<String, Object> result = imageService.generateBackground(null);
            background = result.get("image_path").toString();
        }
        background = background.replace("\\", "/");
        logger.info("Current background image: {}", background);
        return background;
    }
    @PostMapping("/change-background")
    @ResponseBody
    public String changeBackground(){
        Map<String,Object> result = imageService.generateBackground(null);
        String background = result.get("image_path").toString();
        background = background.replace("\\", "/");
        logger.info("Changed background image: {}", background);
        return background;
    }

    @GetMapping("/character")
    @ResponseBody
    public Character getCharacterConfig(@RequestParam(required = false) String characterId) {
        if (characterId == null || characterId.isEmpty()) {
            characterId = "default"; // 默认角色ID
        }
        return characterService.getCharacterById(characterId);
    }
    @GetMapping("/characters")
    @ResponseBody
    public Map<String,Character> getAllCharacters() {
        return characterService.getAllCharacters();
    }
    @GetMapping("/current-character")
    @ResponseBody
    public Character getCurrentCharacterId() {
        return characterService.getCurrentCharacter();
    }
    @PostMapping("/set-character")
    @ResponseBody
    public String setCharacter(@RequestParam String characterId) {
        return characterService.setCurrentCharacterById(characterId)? "success":"failed";
    }

    @GetMapping("/java-version")
    @ResponseBody
    public String javaVersion(){
        return "Java "+System.getProperty("java.version");
    }
    @GetMapping("/generate-options")
    @ResponseBody
    public String[] generateOptions(){
        return optionService.generateOptions();
    }
}
