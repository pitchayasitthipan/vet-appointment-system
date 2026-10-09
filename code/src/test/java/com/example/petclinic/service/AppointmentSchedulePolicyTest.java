package com.example.petclinic.service;

import java.time.*;
import com.example.petclinic.domain.entity.Doctor;
import com.example.petclinic.exception.InvalidAppointmentException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class AppointmentSchedulePolicyTest {
    private final AppointmentSchedulePolicy policy = new AppointmentSchedulePolicy(new ClinicConfigService(),
        Clock.fixed(Instant.parse("2027-01-04T01:00:00Z"), ZoneId.of("Asia/Bangkok")));
    private Doctor doctor(String schedule) { Doctor d = new Doctor(); d.setWorkSchedule(schedule); return d; }
    private LocalDateTime monday(int h, int m) { return LocalDateTime.of(2027, 1, 4, h, m); }

    @Test void acceptsWeekdayRangeAndLastFullSlot() {
        Doctor d = doctor("จันทร์ - ศุกร์: 09:00 - 17:00");
        assertThatCode(() -> policy.validate(d, monday(16, 30))).doesNotThrowAnyException();
        assertThatThrownBy(() -> policy.validate(d, monday(17, 0))).isInstanceOf(InvalidAppointmentException.class);
    }
    @Test void rejectsPastAndUnalignedTimes() {
        Doctor d = doctor("ทุกวัน: 09:00 - 20:00");
        assertThatThrownBy(() -> policy.validate(d, monday(8, 0))).isInstanceOf(InvalidAppointmentException.class);
        assertThatThrownBy(() -> policy.validate(d, monday(9, 15))).isInstanceOf(InvalidAppointmentException.class);
        assertThatThrownBy(() -> policy.validate(d, monday(9, 0).plusSeconds(1))).isInstanceOf(InvalidAppointmentException.class);
    }
    @Test void respectsClosedDaysAndUnknownSchedules() {
        assertThat(policy.slots(doctor("อังคาร - เสาร์: 10:00 - 19:00"), monday(9, 0).toLocalDate())).isEmpty();
        assertThat(policy.slots(doctor(null), monday(9, 0).toLocalDate())).isEmpty();
        assertThat(policy.slots(doctor("ทุกวัน: 99:00 - 17:00"), monday(9, 0).toLocalDate())).isEmpty();
    }
    @Test void clinicClosesEarlierOnSunday() {
        var slots = policy.slots(doctor("ทุกวัน: 09:00 - 20:00"), LocalDate.of(2027, 1, 10));
        assertThat(slots).hasSize(16);
        assertThat(slots.get(slots.size() - 1).toLocalTime()).isEqualTo(LocalTime.of(16, 30));
    }
    @Test void supportsWraparoundRangesAndSplitShifts() {
        assertThat(policy.slots(doctor("ศุกร์ - จันทร์: 09:00 - 10:00"), monday(9, 0).toLocalDate())).hasSize(2);
        var slots = policy.slots(doctor("จันทร์: 09:00 - 10:00; อังคาร: 13:00 - 17:00; จันทร์: 14:00 - 15:00"), monday(9, 0).toLocalDate());
        assertThat(slots).containsExactly(monday(9, 0), monday(9, 30), monday(14, 0), monday(14, 30));
    }
}
