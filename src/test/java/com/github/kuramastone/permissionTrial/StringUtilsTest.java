package com.github.kuramastone.permissionTrial;

import com.github.kuramastone.permissionTrial.utils.StringUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StringUtilsTest {

    @Test
    public void testReadableTimeToMilliseconds() {
        // Basic tests
        assertEquals(4 * 24 * 60 * 60 * 1000L, StringUtils.readableTimeToMilliseconds("4d")); // 4 days
        assertEquals(7 * 60 * 60 * 1000L, StringUtils.readableTimeToMilliseconds("7h")); // 7 hours
        assertEquals(3 * 60 * 1000L, StringUtils.readableTimeToMilliseconds("3m")); // 3 minutes
        assertEquals(15 * 1000L, StringUtils.readableTimeToMilliseconds("15s")); // 15 seconds

        // Full strings
        assertEquals(
            4 * 24 * 60 * 60 * 1000L + 7 * 60 * 60 * 1000L + 3 * 60 * 1000L + 15 * 1000L, 
            StringUtils.readableTimeToMilliseconds("4d7h3m15s")
        );

        // with spaces
        assertEquals(
            4 * 24 * 60 * 60 * 1000L + 2 * 60 * 1000L + 5 * 1000L,
            StringUtils.readableTimeToMilliseconds("4d 2m 5s")
        );

        // Zero milliseconds
        assertEquals(0L, StringUtils.readableTimeToMilliseconds("0"));
    }

    @Test
    public void testReadableTimeToMillisecondsInvalidFormat() {
        // There is no time here, so it should throw an error
        assertThrows(IllegalArgumentException.class, () -> {
            StringUtils.readableTimeToMilliseconds("invalid");
        });

        // Partially invalid strings can be partially read
        assertEquals(24 * 60 * 60 * 1000L, StringUtils.readableTimeToMilliseconds("1d unknown"));
    }

    @Test
    public void testMillisecondsToReadable() {
        // Convert milliseconds to readable
        assertEquals("4d 7h 3m 15s", StringUtils.millisecondsToReadable(
            4 * 24 * 60 * 60 * 1000L + 7 * 60 * 60 * 1000L + 3 * 60 * 1000L + 15 * 1000L)
        );
        
        // Single time units
        assertEquals("1d", StringUtils.millisecondsToReadable(24 * 60 * 60 * 1000L));
        assertEquals("7h", StringUtils.millisecondsToReadable(7 * 60 * 60 * 1000L));
        assertEquals("3m", StringUtils.millisecondsToReadable(3 * 60 * 1000L));
        assertEquals("15s", StringUtils.millisecondsToReadable(15 * 1000L));

        // Zero
        assertEquals("", StringUtils.millisecondsToReadable(0));
    }

    @Test
    public void testRoundTripConversion() {
        // Ensure converting back and forth results in the original value
        String readableTime = "4d 7h 3m 15s";
        long milliseconds = StringUtils.readableTimeToMilliseconds(readableTime);
        String convertedBack = StringUtils.millisecondsToReadable(milliseconds);
        assertEquals(readableTime, convertedBack);

        // Same for another string
        readableTime = "2d 4h 1m 10s";
        milliseconds = StringUtils.readableTimeToMilliseconds(readableTime);
        convertedBack = StringUtils.millisecondsToReadable(milliseconds);
        assertEquals(readableTime, convertedBack);
    }
}
