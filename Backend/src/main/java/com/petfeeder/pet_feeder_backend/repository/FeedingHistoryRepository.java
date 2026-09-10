package com.petfeeder.pet_feeder_backend.repository;


import com.petfeeder.pet_feeder_backend.entity.FeedingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedingHistoryRepository
extends JpaRepository<FeedingHistory, Integer> {

List<FeedingHistory> findByFeedingUserIdOrderByFeedingTimeDesc(
    int feedingUserId
);
}