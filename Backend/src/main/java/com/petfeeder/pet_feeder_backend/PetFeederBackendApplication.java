package com.petfeeder.pet_feeder_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PetFeederBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetFeederBackendApplication.class, args);
	}

}
