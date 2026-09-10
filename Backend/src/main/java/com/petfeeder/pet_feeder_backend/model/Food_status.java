package com.petfeeder.pet_feeder_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "food_status")
public class Food_status {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "food_id")
    private int foodId;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "current_gram")
    private double currentGram;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getCurrentGram() {
        return currentGram;
    }

    public void setCurrentGram(double currentGram) {
        this.currentGram = currentGram;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }


}
