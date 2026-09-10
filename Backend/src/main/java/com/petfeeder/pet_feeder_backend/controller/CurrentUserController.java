package com.petfeeder.pet_feeder_backend.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/current-user")
@CrossOrigin(origins = "*")

public class CurrentUserController {
	private static int currentUserId = 0;

    // Set current user
    @PostMapping("/{userId}")
    public String setCurrentUser(@PathVariable int userId) {

        currentUserId = userId;

        System.out.println("================================");
        System.out.println("Current User ID: " + currentUserId);
        System.out.println("================================");

        return "Current user set to: " + currentUserId;
    }

    // Get current user through API
    @GetMapping
    public int getCurrentUser() {

        return currentUserId;
    }

    // Get current user from other Java classes
    public static int getCurrentUserId() {

        return currentUserId;
    }
}
