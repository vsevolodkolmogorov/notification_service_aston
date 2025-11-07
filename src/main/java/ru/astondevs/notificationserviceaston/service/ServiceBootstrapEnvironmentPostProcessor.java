package ru.astondevs.notificationserviceaston.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class ServiceBootstrapEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String configUrl = environment.getProperty("config.server.url", "http://config-service:8888");
        String appName = environment.getProperty("spring.application.name", "notification-service");
        String profiles = environment.getProperty("spring.profiles.active", "default");

        String url = String.format("%s/config/%s/%s", configUrl, appName, profiles);
        System.out.println("Fetching config from: " + url);

        try {
            Map<String,Object> body = rest.getForObject(url, Map.class);
            if (body == null) return;

            List<Map<String,Object>> propertySources = (List<Map<String,Object>>) body.get("propertySources");
            MutablePropertySources sources = environment.getPropertySources();

            for (int i = propertySources.size() - 1; i >= 0; i--) {
                Map<String,Object> ps = propertySources.get(i);
                Map<String,Object> src = (Map<String,Object>) ps.get("source");

                MapPropertySource prop = new MapPropertySource("remote-config:" + ps.get("name"), src);
                sources.addFirst(prop);

                System.out.println("Added remote property source: " + ps.get("name"));
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load remote config", e);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
