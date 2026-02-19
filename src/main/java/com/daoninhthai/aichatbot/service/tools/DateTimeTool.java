package com.daoninhthai.aichatbot.service.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

/**
 * Date and time tool that provides current date/time and time zone conversions.
 */
@Slf4j
@Component
public class DateTimeTool implements Function<String, String> {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    @Override
    public String apply(String input) {
        log.debug("DateTime tool invoked with input: {}", input);
        try {
            if (input == null || input.isBlank() || "now".equalsIgnoreCase(input.trim())) {
                return getCurrentDateTime();
            }

            // Try to interpret as a time zone ID for conversion
            String timezone = input.trim();
            ZoneId zoneId = ZoneId.of(timezone);
            ZonedDateTime converted = ZonedDateTime.now(zoneId);

            return String.format("Current time in %s: %s", timezone, converted.format(FORMATTER));
        } catch (Exception e) {
            log.warn("Invalid timezone or input: {}", input);
            return getCurrentDateTime() + "\n(Could not parse timezone: " + input + ")";
        }
    }

    private String getCurrentDateTime() {
        ZonedDateTime now = ZonedDateTime.now();
        return String.format("Current date and time: %s (System timezone: %s)",
                now.format(FORMATTER), ZoneId.systemDefault());
    }
}
