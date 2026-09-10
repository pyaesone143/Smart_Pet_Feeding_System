package com.petfeeder.pet_feeder_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableScheduling
public class PetFeederBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetFeederBackendApplication.class, args);
	}
	 @Bean
    CommandLineRunner initUsers(UserRepository userRepository) {
        return args -> {

            if (userRepository.findByEmail("pyae@gmail.com").isEmpty()) {

                User user = new User();

                user.setName("Pyae Sone Khin");
                user.setEmail("pyae@gmail.com");
                user.setPassword("pyaesonekhin");
            

                userRepository.save(user);

                System.out.println(" user created!");
            }
        };
    }

}
