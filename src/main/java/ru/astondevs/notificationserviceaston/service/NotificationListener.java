package ru.astondevs.notificationserviceaston.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import ru.astondevs.notificationserviceaston.dto.UserDto;

import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class NotificationListener {
    private static final Logger log = Logger.getLogger(NotificationListener.class.getName());
    private final MailService mailService;

    @KafkaListener(topics = "user.created", groupId = "notification-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleUserCreated(UserDto userDto, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        log.info("Received UserCreatedEvent for " + userDto.getEmail() + " from partition " + partition);

        mailService.sendSimpleMail(
                userDto.getEmail(),
                "Регистрация аккаунта, " + userDto.getName(),
                "Здравствуйте, " + userDto.getName() + "! Ваш аккаунт на сайте ваш сайт был успешно создан."
        );
    }

    @KafkaListener(topics = "user.deleted", groupId = "notification-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleUserDelete(UserDto userDto, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        log.info("Received UserDeletedEvent for " + userDto.getEmail() + " from partition " + partition);

        mailService.sendSimpleMail(
                userDto.getEmail(),
                "Удаление аккаунта , " + userDto.getName(),
                "Здравствуйте, " + userDto.getName() + "! Ваш аккаунт был удалён."
        );
    }
}
