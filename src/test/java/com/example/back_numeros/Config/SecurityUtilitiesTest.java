package com.example.back_numeros.Config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityUtilitiesTest {

    @Test
    void blocksLoginAfterEightFailuresAndAllowsExplicitReset() {
        LoginAttemptService attempts = new LoginAttemptService();
        String address = "192.0.2.1";

        for (int i = 0; i < 7; i++) {
            attempts.recordFailure(address);
        }
        assertFalse(attempts.isBlocked(address));

        attempts.recordFailure(address);
        assertTrue(attempts.isBlocked(address));

        attempts.clear(address);
        assertFalse(attempts.isBlocked(address));
    }

    @Test
    void boundsPageSizeAndNormalizesNegativeInputs() {
        var pageable = PaginationRequest.bounded(-1, 1000, 10);

        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals(1, PaginationRequest.bounded(0, 0).getPageSize());
    }
}
