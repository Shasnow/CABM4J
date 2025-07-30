package com.github.shasnow.cabm4j.controller;

import com.alibaba.fastjson2.JSONObject;
import com.github.shasnow.cabm4j.entity.Character;
import com.github.shasnow.cabm4j.service.CharacterService;
import com.github.shasnow.cabm4j.service.ChatService;
import com.github.shasnow.cabm4j.service.ImageService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

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
    @PostMapping("/chat")
    @ResponseBody
    public JSONObject chat(@RequestParam String message){
        // 处理聊天消息
        logger.info("Received message: {}", message);
        // 在这里添加实际的流式聊天逻辑
        return chatService.chatCompletions(message);
    }

    @GetMapping(value = "/chat/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<String> chatStream(@RequestParam String message){
        logger.info("Chat stream request received with message: {}", message);
        return chatService.chatCompletionsStream(message);
    }

    @PostMapping("/change-background")
    @ResponseBody
    public Map<String,String> changeBackground(Model model){
        Map<String,Object> result = imageService.generateBackground(null);
        String background = result.get("image_path").toString();
        background = background.replace("\\", "/");
        logger.info("Changed background image: {}", background);
        model.addAttribute("background",background);
        return Map.of("status", "success", "background", background);
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
}
