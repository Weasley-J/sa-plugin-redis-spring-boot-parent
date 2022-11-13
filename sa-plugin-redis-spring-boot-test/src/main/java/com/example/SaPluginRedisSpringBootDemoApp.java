package com.example;

import io.github.weasleyj.satoken.session.annotation.EnableSaIndependentRedisSession;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@EnableSaIndependentRedisSession
@SpringBootApplication
public class SaPluginRedisSpringBootDemoApp {

    public static void main(String[] args) {
        SpringApplication.run(SaPluginRedisSpringBootDemoApp.class, args);
    }

}
