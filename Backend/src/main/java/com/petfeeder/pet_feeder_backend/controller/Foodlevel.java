package com.petfeeder.pet_feeder_backend.controller;

import com.petfeeder.pet_feeder_backend.model.Food_status;
import com.petfeeder.pet_feeder_backend.repository.FoodStatusRepository;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class Foodlevel {
	 private FoodStatusRepository foodStatusRepository;

	    public Foodlevel(FoodStatusRepository foodStatusRepository) {
	        this.foodStatusRepository = foodStatusRepository;
	    }

	    @GetMapping("/api/food")
	    public double getFood() {

	        // Get currently logged-in user's ID
	        int currentUserId =
	                CurrentUserController.getCurrentUserId();

	        // No user logged in
	        if (currentUserId == 0) {
	            return 0;
	        }

	        // Find food status for current user
	        Food_status foodStatus =
	                foodStatusRepository.findByUserId(currentUserId);

	        // No food status found
	        if (foodStatus == null) {
	            return 0;
	        }

	        // Return current food amount
	        return foodStatus.getCurrentGram();
	    }

}
