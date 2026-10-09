package com.example.petclinic.service;

import java.time.*;
import java.util.*;
import java.util.regex.*;
import com.example.petclinic.domain.entity.Doctor;
import com.example.petclinic.exception.InvalidAppointmentException;
import org.springframework.stereotype.Component;

/** Supports Thai day lists/ranges, with one shift per newline/semicolon. Unknown schedules fail closed. */
@Component
public class AppointmentSchedulePolicy {
    public static final int SLOT_MINUTES = 30;
    private static final List<String> DAYS = List.of("จันทร์", "อังคาร", "พุธ", "พฤหัสบดี", "ศุกร์", "เสาร์", "อาทิตย์");
    private static final Pattern HOURS = Pattern.compile("(\\d{2}:\\d{2})\\s*[-–—]\\s*(\\d{2}:\\d{2})");
    private static final String DAY_PATTERN = "(จันทร์|อังคาร|พุธ|พฤหัสบดี|ศุกร์|เสาร์|อาทิตย์)";
    private static final Pattern DAY_RANGE = Pattern.compile(DAY_PATTERN + "\\s*(?:[-–—]|ถึง)\\s*" + DAY_PATTERN);
    private final ClinicConfigService config;
    private final Clock clock;

    public AppointmentSchedulePolicy(ClinicConfigService config, Clock appointmentClock) {
        this.config = config;
        this.clock = appointmentClock;
    }

    public void validate(Doctor doctor, LocalDateTime time) {
        if (time == null || !time.isAfter(LocalDateTime.now(clock))) {
            throw new InvalidAppointmentException("วันเวลานัดหมายต้องอยู่ในอนาคต (เวลาประเทศไทย)");
        }
        if (!isAligned(time)) {
            throw new InvalidAppointmentException("กรุณาเลือกเวลาเป็นช่องละ 30 นาที เช่น 09:00 หรือ 09:30");
        }
        if (!withinHours(clinicHours(time.getDayOfWeek()), time.toLocalTime())) {
            throw new InvalidAppointmentException("เวลานัดหมายอยู่นอกเวลาเปิดคลินิก");
        }
        if (doctor == null || !worksAt(doctor.getWorkSchedule(), time)) {
            throw new InvalidAppointmentException("สัตวแพทย์ไม่มีเวรที่รองรับเวลานี้ กรุณาเลือกเวลาอื่นหรือติดต่อคลินิก");
        }
    }

    public List<LocalDateTime> slots(Doctor doctor, LocalDate date) {
        if (date == null) throw new InvalidAppointmentException("กรุณาระบุวันที่");
        List<LocalDateTime> slots = new ArrayList<>();
        for (int minute = 0; minute < 24 * 60; minute += SLOT_MINUTES) {
            LocalDateTime time = date.atStartOfDay().plusMinutes(minute);
            if (time.isAfter(LocalDateTime.now(clock))
                && withinHours(clinicHours(time.getDayOfWeek()), time.toLocalTime())
                && doctor != null && worksAt(doctor.getWorkSchedule(), time)) slots.add(time);
        }
        return List.copyOf(slots);
    }

    public void requireFuture(LocalDateTime time) {
        if (time == null || !time.isAfter(LocalDateTime.now(clock))) {
            throw new InvalidAppointmentException("ไม่สามารถแก้ไขหรือยกเลิกนัดที่ถึงเวลาแล้วได้");
        }
    }

    private boolean isAligned(LocalDateTime time) {
        return time.getMinute() % SLOT_MINUTES == 0 && time.getSecond() == 0 && time.getNano() == 0;
    }

    private String clinicHours(DayOfWeek day) {
        return switch (day) {
            case SATURDAY -> config.getOpeningHoursSaturday();
            case SUNDAY -> config.getOpeningHoursSunday();
            default -> config.getOpeningHoursWeekdays();
        };
    }

    private boolean worksAt(String schedule, LocalDateTime time) {
        if (schedule == null || schedule.isBlank()) return false;
        for (String shift : schedule.split("[;\\r\\n]+")) {
            Matcher hours = HOURS.matcher(shift);
            if (!hours.find()) continue;
            String days = shift.substring(0, hours.start());
            if (matchesDay(days, time.getDayOfWeek()) && withinHours(shift, time.toLocalTime())) return true;
        }
        return false;
    }

    private boolean matchesDay(String text, DayOfWeek day) {
        if (text.contains("ทุกวัน")) return true;
        int target = day.getValue() - 1;
        Matcher ranges = DAY_RANGE.matcher(text);
        while (ranges.find()) {
            int start = DAYS.indexOf(ranges.group(1));
            int end = DAYS.indexOf(ranges.group(2));
            if (start <= end ? target >= start && target <= end : target >= start || target <= end) return true;
        }
        // Remove range endpoints before interpreting the remaining explicit day list.
        return DAY_RANGE.matcher(text).replaceAll("").contains(DAYS.get(target));
    }

    private boolean withinHours(String hours, LocalTime start) {
        if (hours == null) return false;
        Matcher match = HOURS.matcher(hours);
        if (!match.find()) return false;
        try {
            LocalTime open = LocalTime.parse(match.group(1));
            LocalTime close = LocalTime.parse(match.group(2));
            // Every service reserves one full 30-minute slot; no overnight shifts.
            return close.isAfter(open) && !start.isBefore(open)
                && !start.isAfter(LocalTime.MAX.minusMinutes(SLOT_MINUTES))
                && !start.plusMinutes(SLOT_MINUTES).isAfter(close);
        } catch (DateTimeException e) { return false; }
    }
}
