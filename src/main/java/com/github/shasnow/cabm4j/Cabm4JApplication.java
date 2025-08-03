package com.github.shasnow.cabm4j;

import com.github.shasnow.cabm4j.util.PropertiesManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

@SpringBootApplication
public class Cabm4JApplication {
    public static void main(String[] args) {
        SpringApplication.run(Cabm4JApplication.class, args);
    }

    private static void openBrowser(String url) {
        // 尝试使用 Desktop 类
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
                System.out.println("Browser opened successfully.");
                return;
            } catch (Exception e) {
                System.err.println("Desktop API failed: " + e.getMessage());
            }
        }

        // 回退到系统命令
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec("open " + url);
            } else if (os.contains("nix") || os.contains("nux")) {
                Runtime.getRuntime().exec("xdg-open " + url);
            }
        } catch (IOException e) {
            System.err.println("Command execution failed: " + e.getMessage());
        }
    }

    @Bean
    public ApplicationListener<WebServerInitializedEvent> browserOpener() {
        return event -> {
            int port = event.getWebServer().getPort();
            String ip;
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                ip = "127.0.0.1"; // 默认回退值
            }
            String url = "http://localhost:" + port;

            // 打印访问地址（HTTP）
            System.out.println("\n********************************************************");
            System.out.println("Application is running! Access URLs:");
            System.out.println("Local: \t\t" + url);
            System.out.println("External: \thttp://" + ip + ":" + port);
            System.out.println("********************************************************\n");
            if (Boolean.parseBoolean(PropertiesManager.getProperty("auto-open-browser", "true"))) openBrowser(url);
        };
    }
}
