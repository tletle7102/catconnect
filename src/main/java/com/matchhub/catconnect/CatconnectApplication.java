package com.matchhub.catconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class CatconnectApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatconnectApplication.class, args);
	}

}
