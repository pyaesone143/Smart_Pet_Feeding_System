package com.petfeeder.pet_feeder_backend.controller;


import java.util.Optional;
import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import com.petfeeder.pet_feeder_backend.User;
import com.petfeeder.pet_feeder_backend.UserRepository;
import com.petfeeder.pet_feeder_backend.entity.FeedingHistory;
import com.petfeeder.pet_feeder_backend.repository.FeedingHistoryRepository;



@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FeedingController {

	 private final RestClient restClient;
	    private final UserRepository userRepository;
	    private final FeedingHistoryRepository feedingHistoryRepository;

	    private static final String ESP32_URL =
	            "http://10.245.156.214";

	    public FeedingController(
	            RestClient.Builder builder,
	            UserRepository userRepository,
	            FeedingHistoryRepository feedingHistoryRepository) {

	        this.restClient = builder.build();
	        this.userRepository = userRepository;
	        this.feedingHistoryRepository = feedingHistoryRepository;
	    }

	    // ============================================
	    // FEED NOW
	    // WEB APP → SPRING BOOT → ESP32 → MYSQL
	    // ============================================

	    @PostMapping("/feed")
	    public ResponseEntity<String> feedNow(
	            @RequestBody FeedRequest request) {

	        System.out.println();
	        System.out.println("================================");
	        System.out.println("           FEED NOW");
	        System.out.println("================================");

	        int userId = request.getUser_id();

	        System.out.println("Current User ID: " + userId);

	        // ============================================
	        // 1. FIND USER FROM MYSQL
	        // ============================================

	        Optional<User> userOptional =
	                userRepository.findById(userId);

	        if (userOptional.isEmpty()) {

	            System.out.println("ERROR: User not found.");

	            return ResponseEntity
	                    .badRequest()
	                    .body(
	                            "{\"success\":false," +
	                            "\"status\":\"user_not_found\"}"
	                    );
	        }

	        User user = userOptional.get();

	        // ============================================
	        // 2. GET USER'S MAX FOOD AMOUNT
	        // ============================================

	        double maxFoodAmount =
	                user.getMax_food_amount();

	        System.out.println(
	                "Max Food Amount from MySQL: "
	                        + maxFoodAmount
	                        + " g"
	        );

	        if (maxFoodAmount <= 0) {

	            System.out.println(
	                    "ERROR: Max food amount is not set."
	            );

	            return ResponseEntity
	                    .badRequest()
	                    .body(
	                            "{\"success\":false," +
	                            "\"status\":\"max_food_not_set\"}"
	                    );
	        }

	        try {

	            // ============================================
	            // 3. SEND LIMIT TO ESP32
	            // ============================================

	           
	            // ============================================
	            // 4. START FEEDING
	            // ============================================

	            System.out.println();
	            System.out.println(
	                    "Sending feed command to ESP32..."
	            );

	            String feedResponse =
	                    restClient.post()
	                            .uri(
	                                    ESP32_URL
	                                            + "/feed"
	                            )
	                            .retrieve()
	                            .body(String.class);

	            System.out.println(
	                    "ESP32 /feed response: "
	                            + feedResponse
	            );

	            // ============================================
	            // 5. CHECK THAT FEEDING STARTED
	            // ============================================

	            if (!feedResponse.contains(
	                    "\"feeding_started\"")) {

	                if (feedResponse.contains(
	                        "\"already_feeding\"")) {

	                    System.out.println(
	                            "ESP32 is already feeding."
	                    );

	                    return ResponseEntity
	                            .ok()
	                            .body(
	                                    "{\"success\":false," +
	                                    "\"status\":\"already_feeding\"}"
	                            );
	                }

	                if (feedResponse.contains(
	                        "\"limit_already_reached\"")) {

	                    System.out.println(
	                            "Food limit already reached."
	                    );

	                    return ResponseEntity
	                            .ok()
	                            .body(
	                                    "{\"success\":false," +
	                                    "\"status\":\"limit_already_reached\"}"
	                            );
	                }

	                System.out.println(
	                        "ERROR: ESP32 did not start feeding."
	                );

	                return ResponseEntity
	                        .status(503)
	                        .body(
	                                "{\"success\":false," +
	                                "\"status\":\"feed_start_failed\"," +
	                                "\"esp32_response\":" +
	                                "\"" +
	                                escapeJson(feedResponse) +
	                                "\"}"
	                        );
	            }

	            System.out.println(
	                    "Feeding started successfully."
	            );

	            // ============================================
	            // 6. WAIT FOR ESP32 TO FINISH FEEDING
	            // ============================================

	            double finalAmount =
	                    waitForFeedingToFinish();

	            System.out.println();
	            System.out.println(
	                    "Final Food Amount: "
	                            + finalAmount
	                            + " g"
	            );

	            // ============================================
	            // 7. SAVE FEEDING HISTORY
	            // ============================================

	            FeedingHistory history =
	                    new FeedingHistory();

	            history.setFeedingUserId(userId);

	            history.setFeedingTime(
	                    LocalDateTime.now()
	            );

	            history.setAmount(finalAmount);

	            feedingHistoryRepository.save(history);

	            System.out.println(
	                    "Feeding history saved successfully."
	            );

	            System.out.println(
	                    "================================"
	            );

	            // ============================================
	            // 8. RETURN RESULT TO WEB APP
	            // ============================================

	            return ResponseEntity.ok(
	                    "{\"success\":true," +
	                    "\"status\":\"feeding_completed\"," +
	                    "\"amount\":" +
	                    finalAmount +
	                    "}"
	            );

	        } catch (Exception e) {

	            System.out.println(
	                    "ERROR: Could not communicate with ESP32"
	            );

	            System.out.println(
	                    "Exception type: "
	                            + e.getClass().getName()
	            );

	            System.out.println(
	                    "Message: "
	                            + e.getMessage()
	            );

	            e.printStackTrace();

	            System.out.println(
	                    "================================"
	            );

	            return ResponseEntity
	                    .status(503)
	                    .body(
	                            "{\"success\":false," +
	                            "\"status\":\"esp32_error\"," +
	                            "\"message\":" +
	                            "\"Could not communicate with ESP32\"}"
	                    );
	        }
	    }

	    // ============================================
	    // WAIT FOR FEEDING TO FINISH
	    // ============================================

	    private double waitForFeedingToFinish()
	            throws InterruptedException {

	        System.out.println();
	        System.out.println(
	                "Waiting for ESP32 feeding to finish..."
	        );

	        /*
	         * Check ESP32 every 500 ms.
	         *
	         * Maximum waiting time:
	         * 60 seconds
	         */

	        long startTime =
	                System.currentTimeMillis();

	        long timeout =
	                60_000;

	        double latestWeight = 0.0;

	        while (
	                System.currentTimeMillis()
	                        - startTime
	                        < timeout
	        ) {

	            Thread.sleep(500);

	            try {

	                String statusResponse =
	                        restClient.get()
	                                .uri(
	                                        ESP32_URL
	                                                + "/status"
	                                )
	                                .retrieve()
	                                .body(String.class);

	                System.out.println(
	                        "ESP32 /status: "
	                                + statusResponse
	                );

	                // ========================================
	                // READ WEIGHT
	                // ========================================

	                latestWeight =
	                        extractWeight(statusResponse);

	                // ========================================
	                // CHECK FEEDING STATE
	                // ========================================

	                boolean stillFeeding =
	                        extractFeedingState(
	                                statusResponse
	                        );

	                System.out.println(
	                        "Current Weight: "
	                                + latestWeight
	                                + " g"
	                );

	                System.out.println(
	                        "Feeding: "
	                                + stillFeeding
	                );

	                // ========================================
	                // FEEDING FINISHED
	                // ========================================

	                if (!stillFeeding) {

	                    System.out.println();
	                    System.out.println(
	                            "ESP32 feeding completed."
	                    );

	                    return latestWeight;
	                }

	            } catch (Exception e) {

	                System.out.println(
	                        "WARNING: Could not read "
	                                + "ESP32 status."
	                );

	                System.out.println(
	                        e.getMessage()
	                );
	            }
	        }

	        // ============================================
	        // TIMEOUT
	        // ============================================

	        System.out.println();
	        System.out.println(
	                "ERROR: Feeding timeout."
	        );

	        throw new RuntimeException(
	                "ESP32 feeding did not finish "
	                        + "within 60 seconds."
	        );
	    }

	    // ============================================
	    // EXTRACT WEIGHT
	    // ============================================

	    private double extractWeight(
	            String response) {

	        try {

	            /*
	             * Expected:
	             *
	             * {
	             *   "feeding":false,
	             *   "weight":125.50,
	             *   "limit":125.00
	             * }
	             */

	            String marker =
	                    "\"weight\":";

	            int start =
	                    response.indexOf(marker);

	            if (start == -1) {

	                throw new RuntimeException(
	                        "Weight not found."
	                );
	            }

	            start += marker.length();

	            int end =
	                    response.indexOf(
	                            ",",
	                            start
	                    );

	            if (end == -1) {

	                end =
	                        response.indexOf(
	                                "}",
	                                start
	                        );
	            }

	            String weightText =
	                    response.substring(
	                            start,
	                            end
	                    ).trim();

	            return Double.parseDouble(
	                    weightText
	            );

	        } catch (Exception e) {

	            System.out.println(
	                    "ERROR: Could not read weight "
	                            + "from ESP32 response."
	            );

	            throw new RuntimeException(
	                    "Invalid ESP32 status response: "
	                            + response
	            );
	        }
	    }

	    // ============================================
	    // EXTRACT FEEDING STATE
	    // ============================================

	    private boolean extractFeedingState(
	            String response) {

	        try {

	            String marker =
	                    "\"feeding\":";

	            int start =
	                    response.indexOf(marker);

	            if (start == -1) {

	                throw new RuntimeException(
	                        "Feeding state not found."
	                );
	            }

	            start += marker.length();

	            int end =
	                    response.indexOf(
	                            ",",
	                            start
	                    );

	            if (end == -1) {

	                end =
	                        response.indexOf(
	                                "}",
	                                start
	                        );
	            }

	            String feedingText =
	                    response.substring(
	                            start,
	                            end
	                    ).trim();

	            return Boolean.parseBoolean(
	                    feedingText
	            );

	        } catch (Exception e) {

	            System.out.println(
	                    "ERROR: Could not read feeding state."
	            );

	            throw new RuntimeException(
	                    "Invalid ESP32 status response: "
	                            + response
	            );
	        }
	    }

	    // ============================================
	    // ESCAPE JSON
	    // ============================================

	    private String escapeJson(String text) {

	        if (text == null) {
	            return "";
	        }

	        return text
	                .replace("\\", "\\\\")
	                .replace("\"", "\\\"");
	    }

	    // ============================================
	    // REQUEST BODY
	    // ============================================

	    public static class FeedRequest {

	        private int user_id;

	        public int getUser_id() {

	            return user_id;
	        }

	        public void setUser_id(int user_id) {

	            this.user_id = user_id;
	        }
	    }
}
