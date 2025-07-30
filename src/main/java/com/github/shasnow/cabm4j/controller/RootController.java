package com.github.shasnow.cabm4j.controller;

import com.github.shasnow.cabm4j.service.ImageService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class RootController {
    private static final Logger logger = LoggerFactory.getLogger(RootController.class);
    @Resource
    ImageService imageService;

    @GetMapping("/")
    public String index(Model model) {
        logger.info("GetMapping:index");
        String background = imageService.getCurrentBackground();
        if (background ==null){
            Map<String,Object> result = imageService.generateBackground(null);
            background = result.get("image_path").toString();
        }
        background=background.replace("\\", "/");
        logger.info("Loaded background image:{}", background);
        model.addAttribute("user");
        model.addAttribute("background",background);
        model.addAttribute("characterName","系统");
        model.addAttribute("currentMessage", "欢迎使用CABM4J！请开始您的对话。");
        model.addAttribute("java", System.getProperty("java.version"));
        return "index";
    }
}
