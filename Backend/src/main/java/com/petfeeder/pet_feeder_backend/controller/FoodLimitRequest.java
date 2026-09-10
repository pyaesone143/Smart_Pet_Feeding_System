package com.petfeeder.pet_feeder_backend.controller;

public class FoodLimitRequest {

    private int userId;
    private double max_food_amount;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getMax_food_amount() {
        return max_food_amount;
    }

    public void setMax_food_amount(double max_food_amount) {
        this.max_food_amount = max_food_amount;
    }
}