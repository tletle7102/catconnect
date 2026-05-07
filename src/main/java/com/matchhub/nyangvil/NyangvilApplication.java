package com.matchhub.nyangvil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class NyangvilApplication {

	public static void main(String[] args) {
		SpringApplication.run(NyangvilApplication.class, args);
	}

}
