package com.example.petclinic.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppointmentTimeConfig {
    @Bean
    public Clock appointmentClock() {
        return Clock.system(ZoneId.of("Asia/Bangkok"));
    }
}
