package com.petfeeder.pet_feeder_backend.controller;

public class Food {
	private int foodAmount;
    private String unit;
  

    public Food(int foodAmount, String unit) {
        this.foodAmount = foodAmount;
        this.unit = unit;
      
    }

    public int getFoodAmount() {
        return foodAmount;
    }

    public String getUnit() {
        return unit;
    }

}
