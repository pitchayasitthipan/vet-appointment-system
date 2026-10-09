package com.example.petclinic.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StaffPasscodeServiceTest {

    private static final String CORRECT = "87654321";
    private static final String WRONG = "11111111";
    private static final String CLIENT = "test-client";

    private AtomicReference<Instant> currentTime;
    private StaffPasscodeService service;

    @BeforeEach
    void setUp() {
        currentTime = new AtomicReference<>(
                Instant.parse("2026-10-09T10:00:00Z"));

        Clock clock = new Clock() {
            @Override
            public Instant instant() {
                return currentTime.get();
            }

            @Override
            public ZoneOffset getZone() {
                return ZoneOffset.UTC;
            }

            @Override
            public Clock withZone(java.time.ZoneId zone) {
                return this;
            }
        };

        service = new StaffPasscodeService(CORRECT, clock);
    }

    private void advanceMinutes(long minutes) {
        currentTime.updateAndGet(
                time -> time.plusSeconds(minutes * 60));
    }

    private StaffPasscodeService.Result failThreeTimes() {
        service.verify(CLIENT, WRONG);
        service.verify(CLIENT, WRONG);
        return service.verify(CLIENT, WRONG);
    }

    @Test
    void correctPasscodeShouldUnlock() {
        var result = service.verify(CLIENT, CORRECT);

        assertTrue(result.success());
        assertFalse(result.locked());
    }

    @Test
    void threeWrongAttemptsShouldLockFor15Minutes() {
        assertFalse(service.verify(CLIENT, WRONG).locked());
        assertFalse(service.verify(CLIENT, WRONG).locked());

        var result = service.verify(CLIENT, WRONG);

        assertFalse(result.success());
        assertTrue(result.locked());
        assertEquals(15, result.remainingMinutes());
    }

    @Test
    void correctPasscodeShouldBeRejectedWhileLocked() {
        failThreeTimes();

        var result = service.verify(CLIENT, CORRECT);

        assertFalse(result.success());
        assertTrue(result.locked());
    }

    @Test
    void lockDurationShouldDoubleAfterEachLock() {
        var first = failThreeTimes();
        assertEquals(15, first.remainingMinutes());

        advanceMinutes(15);

        var second = failThreeTimes();
        assertEquals(30, second.remainingMinutes());

        advanceMinutes(30);

        var third = failThreeTimes();
        assertEquals(60, third.remainingMinutes());
    }

    @Test
    void lockDurationShouldNotExceed24Hours() {
        long[] expected = {
                15, 30, 60, 120, 240, 480, 960, 1440, 1440
        };

        for (long minutes : expected) {
            var result = failThreeTimes();
            assertTrue(result.locked());
            assertEquals(minutes, result.remainingMinutes());

            advanceMinutes(minutes);
        }
    }

    @Test
    void successfulLoginShouldResetLockLevel() {
        failThreeTimes();

        advanceMinutes(15);

        var success = service.verify(CLIENT, CORRECT);
        assertTrue(success.success());

        var nextLock = failThreeTimes();
        assertEquals(15, nextLock.remainingMinutes());
    }

    @Test
    void attemptsOutside10MinuteWindowShouldReset() {
        service.verify(CLIENT, WRONG);
        service.verify(CLIENT, WRONG);

        advanceMinutes(10);

        var result = service.verify(CLIENT, WRONG);

        assertFalse(result.locked());
        assertFalse(result.success());
    }

    @Test
    void differentClientsShouldHaveSeparateCounters() {
        failThreeTimes();

        var otherClient =
                service.verify("another-client", CORRECT);

        assertTrue(otherClient.success());
        assertFalse(otherClient.locked());
    }
}