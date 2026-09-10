package com.petfeeder.pet_feeder_backend.repository;

import com.petfeeder.pet_feeder_backend.model.Food_status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodStatusRepository extends JpaRepository<Food_status, Integer> {
	 Food_status findByUserId(int userId);

}
