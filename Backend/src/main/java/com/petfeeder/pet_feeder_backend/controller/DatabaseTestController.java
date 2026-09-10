package com.petfeeder.pet_feeder_backend.controller;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DatabaseTestController {

    private final DataSource dataSource;

    public DatabaseTestController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/db-test")
    public String testDatabase() {

        try (Connection connection = dataSource.getConnection()) {

            return "MySQL connection successful!";

        } catch (Exception e) {

            e.printStackTrace();

            return "MySQL connection failed: " + e.getMessage();
        }
    }
}