package com.example.petclinic.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StaffPasscodeService {

    private static final int MAX_ATTEMPTS = 3;
    private static final Duration ATTEMPT_WINDOW = Duration.ofMinutes(10);
    private static final Duration FIRST_LOCK = Duration.ofMinutes(15);
    private static final Duration MAX_LOCK = Duration.ofHours(24);

    private final String staffPasscode;
    private final Clock clock;

    private final ConcurrentHashMap<String, AttemptState> attempts =
            new ConcurrentHashMap<>();

    @Autowired
    public StaffPasscodeService(
            @Value("${clinic.staff-passcode}") String staffPasscode) {
        this(staffPasscode, Clock.systemUTC());
    }

    // Constructor สำหรับ Unit Test เพื่อจำลองเวลาได้
    StaffPasscodeService(String staffPasscode, Clock clock) {
        if (staffPasscode == null || !staffPasscode.matches("\\d{8}")) {
            throw new IllegalStateException(
                    "STAFF_PASSCODE must contain exactly 8 digits");
        }
        this.staffPasscode = staffPasscode;
        this.clock = clock;
    }

    public Result verify(String clientKey, String submittedPasscode) {
        Instant now = clock.instant();

        return attempts.compute(clientKey, (key, existing) -> {
            AttemptState state =
                    existing == null ? new AttemptState() : existing;

            // ยังอยู่ในช่วงล็อก: ห้ามเข้าแม้รหัสถูก
            if (state.lockedUntil != null
                    && now.isBefore(state.lockedUntil)) {
                long remainingSeconds =
                        Duration.between(now, state.lockedUntil).getSeconds();

                long remainingMinutes =
                        (remainingSeconds + 59) / 60;

                state.result =
                        new Result(false, true, remainingMinutes);
                return state;
            }

            // หมดเวลาล็อกแล้ว เริ่มนับการกรอกผิดรอบใหม่
            if (state.lockedUntil != null) {
                state.lockedUntil = null;
                state.failedAttempts = 0;
                state.windowStartedAt = null;
            }

            // กรอกรหัสถูก: รีเซ็ตทุกอย่าง
            if (staffPasscode.equals(submittedPasscode)) {
                state.failedAttempts = 0;
                state.windowStartedAt = null;
                state.lockLevel = 0;
                state.result = new Result(true, false, 0);
                return state;
            }

            // เริ่มหน้าต่างนับการกรอกผิด 10 นาที
            if (state.windowStartedAt == null
                    || !now.isBefore(
                            state.windowStartedAt.plus(ATTEMPT_WINDOW))) {
                state.windowStartedAt = now;
                state.failedAttempts = 0;
            }

            state.failedAttempts++;

            if (state.failedAttempts >= MAX_ATTEMPTS) {
                // 15, 30, 60, 120... นาที (สูงสุด 24 ชั่วโมง)
                Duration lockDuration = FIRST_LOCK.multipliedBy(
                        1L << Math.min(state.lockLevel, 7));

                if (lockDuration.compareTo(MAX_LOCK) > 0) {
                    lockDuration = MAX_LOCK;
                }

                state.lockedUntil = now.plus(lockDuration);
                state.lockLevel++;
                state.failedAttempts = 0;
                state.windowStartedAt = null;

                state.result = new Result(
                        false, true, lockDuration.toMinutes());
            } else {
                state.result = new Result(false, false, 0);
            }

            return state;
        }).result;
    }

    public record Result(
            boolean success,
            boolean locked,
            long remainingMinutes) {
    }

    private static class AttemptState {
        int failedAttempts;
        int lockLevel;
        Instant windowStartedAt;
        Instant lockedUntil;
        Result result;
    }
}