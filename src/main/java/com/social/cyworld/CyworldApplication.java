package com.social.cyworld;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class CyworldApplication {

	public static void main(String[] args) {
		SpringApplication.run(CyworldApplication.class, args);
	}

}
