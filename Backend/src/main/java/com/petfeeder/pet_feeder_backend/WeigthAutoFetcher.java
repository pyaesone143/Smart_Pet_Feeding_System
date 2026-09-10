package com.petfeeder.pet_feeder_backend;

import com.petfeeder.pet_feeder_backend.controller.CurrentUserController;
import com.petfeeder.pet_feeder_backend.model.Food_status;
import com.petfeeder.pet_feeder_backend.repository.FoodStatusRepository;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WeigthAutoFetcher {
	 private  RestClient restClient;

	 private FoodStatusRepository foodStatusRepository;

	   // ESP32 URL
    private static final String ESP32_URL =
            "http://10.245.156.214";



	    public WeigthAutoFetcher(
	            RestClient.Builder builder,
	            FoodStatusRepository foodStatusRepository) {

	        this.restClient = builder.build();
	        this.foodStatusRepository = foodStatusRepository;
	    }

	    @Scheduled(fixedRate = 1000)
	    public void getWeightAutomatically() {

	        try {

	        	// ============================================
	            // GET CURRENT USER ID
	            // ============================================

	            int currentUserId =
	                    CurrentUserController.getCurrentUserId();

	            // No user logged in
	            if (currentUserId == 0) {

	                System.out.println(
	                        "No current user. Weight will not be saved.");

	                return;
	            }

	            System.out.println("================================");
	            System.out.println(
	                    "Current User ID: " + currentUserId);


	            // ============================================
	            // GET WEIGHT FROM ESP32
	            // ============================================

	            String response = restClient.get()
	                    .uri(ESP32_URL + "/weight")
	                    .retrieve()
	                    .body(String.class);

	            System.out.println(
	                    "Weight received from ESP32:");

	            System.out.println(response);


	            // ============================================
	            // EXTRACT WEIGHT FROM JSON
	            // ============================================

	            String weightText = response
	                    .replace("{\"weight\":", "")
	                    .replace("}", "");

	            double weight =
	                    Double.parseDouble(weightText);


	            // ============================================
	            // FIND FOOD STATUS FOR CURRENT USER
	            // ============================================

	            Food_status foodStatus =
	                    foodStatusRepository
	                            .findByUserId(currentUserId);


	            // ============================================
	            // SAVE WEIGHT TO MYSQL
	            // ============================================

	            if (foodStatus == null) {

	                // No food_status row exists yet
	                // Create a new one for the current user

	                foodStatus = new Food_status();

	                foodStatus.setUserId(currentUserId);
	                foodStatus.setCurrentGram(weight);
	                foodStatus.setUpdatedAt(LocalDateTime.now());

	                foodStatusRepository.save(foodStatus);

	                System.out.println("New food status created.");
	                System.out.println("User ID: " + currentUserId);
	                System.out.println("Current gram: " + weight);

	            } else {

	                // Food_status already exists
	                // Update the existing row

	                foodStatus.setCurrentGram(weight);
	                foodStatus.setUpdatedAt(LocalDateTime.now());

	                foodStatusRepository.save(foodStatus);

	                System.out.println("Food status updated.");
	                System.out.println("User ID: " + currentUserId);
	                System.out.println("Current gram: " + weight);
	            }
	        } catch (Exception e) {

	            System.out.println(
	                    "Failed to get/save weight: "
	                    + e.getMessage());
	        }
	    }
}
