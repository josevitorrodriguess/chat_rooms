package com.josevitor.chatrooms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.josevitor.chatrooms.config.JwtProperties;
import com.josevitor.chatrooms.config.PresenceProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, PresenceProperties.class})
@EnableScheduling
public class ChatroomsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatroomsApplication.class, args);
	}

}
