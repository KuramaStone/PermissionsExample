package com.github.kuramastone.permissionTrial.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {


    /**
     * Convert a time such as "4d7h3m15s" into milliseconds
     *
     * @param string Time as a formatted string
     * @return Time in milliseconds
     */
    public static long readableTimeToMilliseconds(String string) {
        try {
            // read it as milliseconds first
            return Long.parseLong(string);
        }
        catch (NumberFormatException e) {
            // try reading the pretty string
        }

        string = string.replace(" ", ""); // Remove any spaces

        Pattern pattern = Pattern.compile("(\\d+)([dhms])");
        Matcher matcher = pattern.matcher(string);

        long totalMilliseconds = 0;
        int matches = 0;
        while (matcher.find()) {
            matches++;
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);

            switch (unit) {
                case "d":
                    totalMilliseconds += value * 24 * 60 * 60 * 1000; // Days to milliseconds
                    break;
                case "h":
                    totalMilliseconds += value * 60 * 60 * 1000; // Hours to milliseconds
                    break;
                case "m":
                    totalMilliseconds += value * 60 * 1000; // Minutes to milliseconds
                    break;
                case "s":
                    totalMilliseconds += value * 1000; // Seconds to milliseconds
                    break;
            }
        }

        if(matches == 0) {
            throw new IllegalArgumentException("Unable to find readable time string in String.");
        }

        return totalMilliseconds;
    }

    /**
     * Convert milliseconds to a format such as "4d 7h 3m 15s"
     *
     * @param expirationTime Time in milliseconds
     * @return Readable time format
     */
    public static String millisecondsToReadable(long expirationTime) {
        long seconds = expirationTime / 1000;
        long days = seconds / (24 * 3600);
        seconds %= (24 * 3600);
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;

        // Building the formatted string
        StringBuilder readableTime = new StringBuilder();
        if (days > 0) {
            readableTime.append(days).append("d ");
        }
        if (hours > 0) {
            readableTime.append(hours).append("h ");
        }
        if (minutes > 0) {
            readableTime.append(minutes).append("m ");
        }
        if (seconds > 0) {
            readableTime.append(seconds).append("s");
        }

        return readableTime.toString().trim();
    }
}
