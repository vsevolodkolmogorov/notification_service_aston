package ru.astondevs.notificationserviceaston.service;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import ru.astondevs.notificationserviceaston.dto.UserEventDto;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(partitions = 1, topics = {"user.created", "user.deleted"})
class NotificationListenerIntegrationTest {

    @Autowired(required = false)
    private EmbeddedKafkaBroker embeddedKafka;

    @MockBean
    private MailService mailService;

    private KafkaTemplate<String, UserEventDto> kafkaTemplate;

    @BeforeEach
    void setup() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);

        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        DefaultKafkaProducerFactory<String, UserEventDto> pf =
                new DefaultKafkaProducerFactory<>(producerProps);
        kafkaTemplate = new KafkaTemplate<>(pf);

        embeddedKafka.brokerProperties(Collections.singletonMap("auto.create.topics.enable", "true"));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void shouldSendMailWhenUserCreatedEventReceived() throws Exception {
        UserEventDto user = new UserEventDto();
        user.setEmail("john@example.com");
        user.setName("John");

        kafkaTemplate.send("user.created", user.getEmail(), user);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                verify(mailService).sendSimpleMail(
                        eq("john@example.com"),
                        contains("Регистрация"),
                        contains("John")
                )
        );
    }


    @Test
    void shouldSendMailWhenUserDeletedEventReceived() throws Exception {
        UserEventDto user = new UserEventDto();
        user.setEmail("jane@example.com");
        user.setName("Jane");

        kafkaTemplate.send("user.deleted", user.getEmail(), user);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                verify(mailService).sendSimpleMail(
                        eq("jane@example.com"),
                        contains("Удаление"),
                        contains("Jane")
                )
        );
    }
}