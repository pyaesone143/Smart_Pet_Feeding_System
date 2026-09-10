package com.petfeeder.pet_feeder_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "feeding_history")

public class FeedingHistory {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feeding_id")
    private int feedingId;

    @Column(name = "feeding_user_id", nullable = false)
    private int feedingUserId;

	 @ManyToOne
    @JoinColumn(
        name = "feeding_user_id",
        referencedColumnName = "user_id",
        insertable = false,
        updatable = false
    )
    private User user;


    @Column(name = "feeding_time")
    private LocalDateTime feedingTime;

    @Column(name = "amount", nullable = false)
    private double amount;


    // Default Constructor
    public FeedingHistory() {
    }


    // Constructor
    public FeedingHistory(int feedingUserId,
                          LocalDateTime feedingTime,
                          double amount) {

        this.feedingUserId = feedingUserId;
        this.feedingTime = feedingTime;
        this.amount = amount;
    }


    // Getter and Setter for feedingId
    public int getFeedingId() {
        return feedingId;
    }

    public void setFeedingId(int feedingId) {
        this.feedingId = feedingId;
    }


    // Getter and Setter for feedingUserId
    public int getFeedingUserId() {
        return feedingUserId;
    }

    public void setFeedingUserId(int feedingUserId) {
        this.feedingUserId = feedingUserId;
    }


    // Getter and Setter for feedingTime
    public LocalDateTime getFeedingTime() {
        return feedingTime;
    }

    public void setFeedingTime(LocalDateTime feedingTime) {
        this.feedingTime = feedingTime;
    }


    // Getter and Setter for amount
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

}
