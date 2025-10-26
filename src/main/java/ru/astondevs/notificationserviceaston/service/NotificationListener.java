package ru.astondevs.notificationserviceaston.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import ru.astondevs.notificationserviceaston.dto.UserEventDto;

import static org.apache.kafka.common.requests.DeleteAclsResponse.log;

@Service
@RequiredArgsConstructor
public class NotificationListener {
    private final NotificationService notificationService;

    @KafkaListener(topics = "user.event", groupId = "notification-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleUserCreated(UserEventDto userEventDto, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        log.info("Received event " + userEventDto.getEventType() + " for " + userEventDto.getEmail() + " from partition " + partition);
        notificationService.processUserEvent(userEventDto);
    }
}