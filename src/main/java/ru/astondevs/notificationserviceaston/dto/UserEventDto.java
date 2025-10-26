package ru.astondevs.notificationserviceaston.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserEventDto {
    private Long id;
    private String name;
    private String email;
    private int age;
    private Long role_id;
    private String eventType;

    @Override
    public String toString() {
        return "UserEventDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", role_id=" + role_id +
                ", eventType='" + eventType + '\'' +
                '}';
    }
}
