package com.github.shasnow.cabm4j.controller;

import com.github.shasnow.cabm4j.service.ImageService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
            Object result = imageService.generateBackground(null);
        }
        model.addAttribute("background",background);
        return "index";
    }
}
