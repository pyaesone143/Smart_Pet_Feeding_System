package com.petfeeder.pet_feeder_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalTime;

@Entity
@Table(name = "food_status")
public class FoodStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "food_id")
    private int foodId;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(name = "current_gram", nullable = false)
    private double currentGram;

    @Column(name = "updated_at", nullable = false)
    private LocalTime updatedAt;


    // Default Constructor
    public FoodStatus() {
    }


    // Constructor
    public FoodStatus(int userId, double currentGram, LocalTime updatedAt) {
        this.userId = userId;
        this.currentGram = currentGram;
        this.updatedAt = updatedAt;
    }


    // Getter and Setter for foodId
    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }


    // Getter and Setter for userId
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }


    // Getter and Setter for currentGram
    public double getCurrentGram() {
        return currentGram;
    }

    public void setCurrentGram(double currentGram) {
        this.currentGram = currentGram;
    }


    // Getter and Setter for updatedAt
    public LocalTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
