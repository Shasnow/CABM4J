package com.github.shasnow.cabm4j.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {
    private static final Logger logger = LoggerFactory.getLogger(RootController.class);

    @GetMapping("/")
    public String index() {
        logger.info("GetMapping:index");
        return "index";
    }
}
