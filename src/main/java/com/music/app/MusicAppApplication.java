package com.music.app;

import com.music.app.entity.User;
import com.music.app.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class MusicAppApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(MusicAppApplication.class, args);
	}


	@Bean
	public RestTemplate restTemplateBean() {
		return new RestTemplate();
	}

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Override
	public void run(String... args) throws Exception {
		User user = userRepo.findByUsername("admin");
		if(user == null){
			User admin = new User();
			admin.setUsername("admin");
			admin.setPassword(bCryptPasswordEncoder.encode("admin"));
			admin.setRole(User.Role.ADMIN);
			admin.setDisplayName("Admin");
			userRepo.save(admin);
		}

	}
}
