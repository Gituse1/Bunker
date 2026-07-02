package com.example.bunker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class BunkerApplication {

	public static void main(String[] args) {
        try {
            SpringApplication.run(BunkerApplication.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

}
