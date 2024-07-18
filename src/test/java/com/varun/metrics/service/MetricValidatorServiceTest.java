package com.varun.metrics.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MetricValidatorServiceTest {

    private MetricValidatorService validator;

    @BeforeEach
    public void setUp() {
        this.validator = new MetricValidatorService();
    }

    @Test
    public void testValidate_validData() {
        assertTrue(validator.validate("{\"video_player\": \"test-123\", \"bitrate\": 4500, \"utc_minute\": 123}"));
        assertTrue(validator.validate("{\"video_player\": \"test-123\", \"framerate\": 45, \"utc_minute\": 123}"));
    }

    @Test
    public void testValidate_invalidJsonObject() {
        // invalid, null or empty json string
        assertFalse(validator.validate(null));
        assertFalse(validator.validate(""));
        assertFalse(validator.validate("\"test\""));

        // unknown json
        assertFalse(validator.validate("{\"test\":\"1\"}"));

        // utc_minute not a number
        assertFalse(validator.validate("{\"video_player\": \"test-123\", \"framerate\": 4500, \"utc_minute\": \"test\"}"));

        // no video_player
        assertFalse(validator.validate("{\"framerate\": 4500, \"utc_minute\": 12345}"));

        // no utc minute
        assertFalse(validator.validate("{\"video_player\": \"test-123\", \"framerate\": 4500}"));

        // invalid bitrate value
        assertFalse(validator.validate("{\"video_player\": \"test-123\", \"bitrate\": \"test\", \"utc_minute\": 12345}"));

        // invalid framerate value
        assertFalse(validator.validate("{\"video_player\": \"test-123\", \"framerate\": \"test\", \"utc_minute\": 12345}"));
    }
}