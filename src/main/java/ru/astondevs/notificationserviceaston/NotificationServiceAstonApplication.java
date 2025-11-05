package ru.astondevs.notificationserviceaston;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NotificationServiceAstonApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceAstonApplication.class, args);
    }

}
