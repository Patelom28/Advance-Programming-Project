package com.example.foodrescue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// Entry point - run this class (green arrow) and open http://localhost:8081
@SpringBootApplication
@EnableScheduling // needed for the automatic expiry sweep (ExpirySweepService)
public class FoodRescueApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodRescueApplication.class, args);
    }
}
