package com.petfeeder.pet_feeder_backend;

import org.springframework.data.jpa.repository.JpaRepository;

public interface  UserRepository extends JpaRepository<User, Integer>  {
	
	User findByEmail(String email);
	

}
