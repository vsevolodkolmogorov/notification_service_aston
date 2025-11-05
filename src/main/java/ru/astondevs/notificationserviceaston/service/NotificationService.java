package ru.astondevs.notificationserviceaston.service;

import org.springframework.stereotype.Service;
import ru.astondevs.notificationserviceaston.common.CircuitBreaker;
import ru.astondevs.notificationserviceaston.common.CircuitBreakerRegistry;
import ru.astondevs.notificationserviceaston.dto.UserEventDto;


@Service
public class NotificationService {

    private final MailService mailService;
    private final CircuitBreaker breaker;

    public NotificationService(MailService mailService, CircuitBreakerRegistry registry) {
        this.mailService = mailService;
        this.breaker = registry.getBreaker("mailCB");
    }

    public void processUserEvent(UserEventDto eventDto) {
        switch (eventDto.getEventType()) {
            case "CREATED" -> sendRegistrationMail(eventDto);
            case "DELETED" -> sendDeletionMail(eventDto);
            default -> throw new IllegalArgumentException("Unknown event type: " + eventDto.getEventType());
        }
    }

    public void sendRegistrationMail(UserEventDto user) {
        executeWithBreaker(user, () -> mailService.sendSimpleMail(
                user.getEmail(),
                "Регистрация аккаунта, " + user.getName(),
                "Здравствуйте, " + user.getName() + "! Ваш аккаунт успешно создан."
        ));
    }

    public void sendDeletionMail(UserEventDto user) {
        executeWithBreaker(user, () -> mailService.sendSimpleMail(
                user.getEmail(),
                "Удаление аккаунта, " + user.getName(),
                "Здравствуйте, " + user.getName() + "! Ваш аккаунт был удалён."
        ));
    }

    private void executeWithBreaker(UserEventDto user, Runnable action) {
        if (!breaker.allowRequest()) {
            fallbackSendMail(user, new RuntimeException("Circuit is OPEN"));
            return;
        }

        try {
            action.run();
            breaker.recordSuccess();
        } catch (Exception e) {
            breaker.recordFailure();
            fallbackSendMail(user, e);
        }
    }

    public void fallbackSendMail(UserEventDto user, Throwable t) {
        System.out.println("Не удалось отправить письмо пользователю " + user.getEmail() + ". Причина: " + t.getMessage());
    }
}
