package com.petfeeder.pet_feeder_backend.controller;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.petfeeder.pet_feeder_backend.User;
import com.petfeeder.pet_feeder_backend.UserRepository;

@RestController
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class UserController_db {
	private final UserRepository userRepository;

    public UserController_db(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

 // GET ALL USERS
    @GetMapping("/api/users")
    public List<User> getUsers() {
        return userRepository.findAll();
    }
    
    // SIGN UP
    @PostMapping("/api/auth/signup")
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }
    
 // UPDATE MAX FOOD AMOUNT
    @PutMapping("/api/users/{userId}/max-food")
    public User updateMaxFoodAmount(
            @PathVariable int userId,
            @RequestBody User userData) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setMax_food_amount(userData.getMax_food_amount());

        return userRepository.save(user);
    }
    

}
