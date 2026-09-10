package com.petfeeder.pet_feeder_backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.petfeeder.pet_feeder_backend.entity.FeedingHistory;
import com.petfeeder.pet_feeder_backend.repository.FeedingHistoryRepository;


@RestController
@RequestMapping("/api/history")
@CrossOrigin(origins = "*")

public class HistoryController {
	private final FeedingHistoryRepository feedingHistoryRepository;

    public HistoryController(
            FeedingHistoryRepository feedingHistoryRepository) {

        this.feedingHistoryRepository = feedingHistoryRepository;
    }

    // ============================================
    // GET USER FEEDING HISTORY
    // ============================================

    @GetMapping("/user/{userId}")
    public List<FeedingHistory> getUserHistory(
            @PathVariable int userId) {

        System.out.println();
        System.out.println("================================");
        System.out.println("       GET FEEDING HISTORY");
        System.out.println("================================");

        System.out.println(
                "User ID: " + userId
        );

        List<FeedingHistory> history =
                feedingHistoryRepository
                        .findByFeedingUserIdOrderByFeedingTimeDesc(
                                userId
                        );

        System.out.println(
                "History records found: "
                        + history.size()
        );

        System.out.println(
                "================================"
        );

        return history;
    }
}
