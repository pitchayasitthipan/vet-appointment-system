package com.example.petclinic.exception;

import java.time.*;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import static org.assertj.core.api.Assertions.*;

class AppointmentExceptionHandlerTest {
    @Test void providerUsesConfiguredClockWhenAvailable() {
        Clock fixed = Clock.fixed(Instant.parse("2026-10-09T00:00:00Z"), ZoneId.of("Asia/Bangkok"));
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(Clock.class, () -> fixed);
            context.register(AppointmentExceptionHandler.class);
            context.refresh();
            var body = context.getBean(AppointmentExceptionHandler.class)
                .invalid(new InvalidAppointmentException("test")).getBody();
            assertThat(body.get("timestamp")).isEqualTo(LocalDateTime.of(2026, 10, 9, 7, 0));
        }
    }
}
