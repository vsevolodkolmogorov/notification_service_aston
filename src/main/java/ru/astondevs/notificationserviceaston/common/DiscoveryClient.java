package ru.astondevs.notificationserviceaston.common;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@EnableScheduling
public class DiscoveryClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${discovery.server.url}")
    private String discoveryServerUrl;

    @Value("${spring.application.name}")

    private String serviceName;
    @Value("${server.port}")
    private int port;

    private final String instanceId = UUID.randomUUID().toString();
    private String instanceUrl;

    @PostConstruct
    public void register() {
        instanceUrl = "http://" + serviceName + ":" + port;
        Map<String, Object> body = new HashMap<>();
        body.put("serviceName", serviceName);
        body.put("instanceId", instanceId);
        body.put("url", instanceUrl);
        body.put("ttlSeconds", 30);

        try {
            restTemplate.postForEntity(discoveryServerUrl + "/services/register", body, Void.class);
            System.out.println("[DiscoveryClient] Registered: " + serviceName + " (" + instanceId + ")");
        } catch (Exception e) {
            System.err.println("[DiscoveryClient] Failed to register: " + e.getMessage());
            throw new RuntimeException();
        }
    }

    @Scheduled(fixedDelay = 10000)
    public void sendHeartbeat() {
        Map<String, String> body = new HashMap<>();
        body.put("instanceId", instanceId);
        try {
            restTemplate.postForEntity(discoveryServerUrl + "/services/heartbeat", body, Void.class);
            System.out.println("[DiscoveryClient] Heartbeat sent: " + serviceName + " (" + instanceId + ")");
        } catch (Exception e) {
            System.err.println("[DiscoveryClient] Heartbeat failed: " + e.getMessage());
            throw new RuntimeException();
        }
    }

    @PreDestroy
    public void deregister() {
        try {
            restTemplate.delete(discoveryServerUrl + "/services/deregister/" + instanceId);
            System.out.println("[DiscoveryClient] Deregistered: " + serviceName);
        } catch (Exception e) {
            System.err.println("[DiscoveryClient] Deregister failed: " + e.getMessage());
            throw new RuntimeException();
        }
    }
}
