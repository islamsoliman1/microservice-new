package com.programming.techie;



import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NotificationServiceApplication {
    static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);

    }
@kafkaListener(topic="notificationTopic")
    public void handleNotification(OrderPlacedEvent orderPlacedEvent){



}



}
