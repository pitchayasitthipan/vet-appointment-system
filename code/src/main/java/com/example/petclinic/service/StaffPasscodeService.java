
package com.example.petclinic.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

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

    // เก็บเฉพาะสถานะการกรอกรหัสผิดและการล็อก
    private final ConcurrentHashMap<String, AttemptState> attempts =
            new ConcurrentHashMap<>();

    @Autowired
    public StaffPasscodeService(
            @Value("${clinic.staff-passcode}") String staffPasscode) {
        this(staffPasscode, Clock.systemUTC());
    }

    // Constructor สำหรับ Unit Test เพื่อจำลองเวลา
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

        // เก็บผลลัพธ์เฉพาะการเรียก verify() ครั้งนี้
        // ไม่เก็บ Result ไว้ใน AttemptState ของ Map
        AtomicReference<Result> resultHolder = new AtomicReference<>();

        attempts.compute(clientKey, (key, existing) -> {
            AttemptState state =
                    existing == null ? new AttemptState() : existing;

            // ยังถูกล็อกอยู่ แม้กรอกรหัสถูกก็เข้าไม่ได้
            if (state.lockedUntil != null
                    && now.isBefore(state.lockedUntil)) {

                long remainingSeconds =
                        Duration.between(now, state.lockedUntil).getSeconds();

                long remainingMinutes =
                        (remainingSeconds + 59) / 60;

                resultHolder.set(
                        new Result(false, true, remainingMinutes));

                return state;
            }

            // หมดเวลาล็อกแล้ว เริ่มนับการกรอกรหัสผิดรอบใหม่
            if (state.lockedUntil != null) {
                state.lockedUntil = null;
                state.failedAttempts = 0;
                state.windowStartedAt = null;
            }

            // กรอกรหัสถูก รีเซ็ตจำนวนครั้งและระดับการล็อก
            if (staffPasscode.equals(submittedPasscode)) {
                resultHolder.set(new Result(true, false, 0));

                // ลบสถานะเดิมออกจาก Map
                return null;
            }

            // นับการกรอกรหัสผิดภายในช่วงเวลา 10 นาที
            if (state.windowStartedAt == null
                    || !now.isBefore(
                            state.windowStartedAt.plus(ATTEMPT_WINDOW))) {

                state.windowStartedAt = now;
                state.failedAttempts = 0;
            }

            state.failedAttempts++;

            if (state.failedAttempts >= MAX_ATTEMPTS) {

                // ระยะเวลาล็อก 15, 30, 60, 120... นาที
                Duration lockDuration = FIRST_LOCK.multipliedBy(
                        1L << Math.min(state.lockLevel, 7));

                // ระยะเวลาล็อกสูงสุด 24 ชั่วโมง
                if (lockDuration.compareTo(MAX_LOCK) > 0) {
                    lockDuration = MAX_LOCK;
                }

                state.lockedUntil = now.plus(lockDuration);
                state.lockLevel++;
                state.failedAttempts = 0;
                state.windowStartedAt = null;

                resultHolder.set(new Result(
                        false,
                        true,
                        lockDuration.toMinutes()));
            } else {
                resultHolder.set(new Result(false, false, 0));
            }

            return state;
        });

        return resultHolder.get();
    }

    public record Result(
            boolean success,
            boolean locked,
            long remainingMinutes) {
    }

    // เก็บเฉพาะสถานะ ไม่เก็บผลการตรวจรหัสครั้งล่าสุด
    private static class AttemptState {
        int failedAttempts;
        int lockLevel;
        Instant windowStartedAt;
        Instant lockedUntil;
    }
}
