package com.petfeeder.pet_feeder_backend.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import com.petfeeder.pet_feeder_backend.User;
import com.petfeeder.pet_feeder_backend.UserRepository;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FoodLimitController {

    private final RestClient restClient;
    private final UserRepository userRepository;

    private static final String ESP32_URL =
            "http://10.245.156.214";

    public FoodLimitController(
            RestClient.Builder builder,
            UserRepository userRepository) {

        this.restClient = builder.build();
        this.userRepository = userRepository;
    }

    // ============================================
    // SET FOOD LIMIT
    // FRONTEND → SPRING BOOT → MYSQL + ESP32
    // ============================================

    @PostMapping("/food-limit")
    public ResponseEntity<String> setFoodLimit(
            @RequestBody FoodLimitRequest request) {

        System.out.println();
        System.out.println("================================");
        System.out.println("        SET FOOD LIMIT");
        System.out.println("================================");

        int userId = request.getUserId();

        double amount = request.getMax_food_amount();

        System.out.println(
                "Current User ID: " + userId
        );

        System.out.println(
                "Food limit received: "
                        + amount
                        + " g"
        );

        // ============================================
        // 1. FIND USER FROM MYSQL
        // ============================================

        Optional<User> userOptional =
                userRepository.findById(userId);

        if (userOptional.isEmpty()) {

            System.out.println(
                    "ERROR: User not found."
            );

            return ResponseEntity
                    .badRequest()
                    .body(
                            "{\"success\":false," +
                            "\"status\":\"user_not_found\"}"
                    );
        }

        User user = userOptional.get();

        // ============================================
        // 2. SAVE FOOD LIMIT TO MYSQL
        // ============================================

        user.setMax_food_amount(amount);

        userRepository.save(user);

        System.out.println(
                "Saved to MySQL: "
                        + user.getMax_food_amount()
                        + " g"
        );

        try {

            // ============================================
            // 3. SEND FOOD LIMIT TO ESP32
            // ============================================

            System.out.println(
                    "Sending food limit to ESP32..."
            );

            String limitRequest =
                    "{\"max_food_amount\":"
                            + amount
                            + "}";

            String limitResponse =
                    restClient.post()
                            .uri(
                                    ESP32_URL
                                            + "/set-limit"
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .body(limitRequest)
                            .retrieve()
                            .body(String.class);

            System.out.println(
                    "ESP32 /set-limit response: "
                            + limitResponse
            );

            // ============================================
            // 4. RETURN SUCCESS
            // ============================================

            System.out.println(
                    "Food limit set successfully."
            );

            System.out.println(
                    "================================"
            );

            return ResponseEntity.ok(
                    "{\"success\":true," +
                    "\"status\":\"food_limit_saved\"," +
                    "\"user_id\":" +
                    userId +
                    "," +
                    "\"max_food_amount\":" +
                    amount +
                    "}"
            );

      } catch (Exception e) {

    System.out.println(
            "ERROR: Could not communicate with ESP32."
    );

    System.out.println(
            "Exception: "
                    + e.getMessage()
    );

    /*
     * MySQL was already updated.
     * ESP32 communication failed.
     *
     * The new food limit is still valid,
     * so send the saved value back to frontend.
     */

    return ResponseEntity.ok(
            "{\"success\":true," +
            "\"status\":\"food_limit_saved_esp32_error\"," +
            "\"user_id\":" +
            userId +
            "," +
            "\"max_food_amount\":" +
            amount +
            "," +
            "\"esp32_connected\":false}"
    );
}
    }
}