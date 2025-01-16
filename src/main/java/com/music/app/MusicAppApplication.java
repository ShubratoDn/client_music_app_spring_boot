package com.music.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class MusicAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(MusicAppApplication.class, args);
	}


	@Bean
	public RestTemplate restTemplateBean() {
		return new RestTemplate();
	}

}
