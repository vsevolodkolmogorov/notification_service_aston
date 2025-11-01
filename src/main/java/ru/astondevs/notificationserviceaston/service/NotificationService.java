package ru.astondevs.notificationserviceaston.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.astondevs.notificationserviceaston.dto.UserEventDto;


@Service
@RequiredArgsConstructor
public class NotificationService {
    private final MailService mailService;

    public void processUserEvent(UserEventDto eventDto) {
        switch (eventDto.getEventType()) {
            case "CREATED" -> sendRegistrationMail(eventDto);
            case "DELETED" -> sendDeletionMail(eventDto);
            default -> throw new IllegalArgumentException("Unknown event type: " + eventDto.getEventType());
        }
    }

    @CircuitBreaker(name = "mailCB", fallbackMethod = "fallbackSendMail")
    public void sendRegistrationMail(UserEventDto user) {
        mailService.sendSimpleMail(
                user.getEmail(),
                "Регистрация аккаунта, " + user.getName(),
                "Здравствуйте, " + user.getName() + "! Ваш аккаунт успешно создан."
        );
    }

    @CircuitBreaker(name = "mailCB", fallbackMethod = "fallbackSendMail")
    public void sendDeletionMail(UserEventDto user) {
        mailService.sendSimpleMail(
                user.getEmail(),
                "Удаление аккаунта, " + user.getName(),
                "Здравствуйте, " + user.getName() + "! Ваш аккаунт был удалён."
        );
    }

    public void fallbackSendMail(UserEventDto user, Throwable t) {
        System.out.println("Не удалось отправить письмо пользователю " + user.getEmail() + ". Причина: " + t.getMessage());
    }
}
