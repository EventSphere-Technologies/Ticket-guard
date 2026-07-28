package com.ticketguard.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

public class FlexibleLocalTimeDeserializer extends JsonDeserializer<LocalTime> {
    private static final DateTimeFormatter[] FORMATTERS = {
        DateTimeFormatter.ISO_LOCAL_TIME, // "19:00:00", "19:00"
        new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("h:mm a")
            .toFormatter(Locale.ENGLISH), // "7:00 PM", "7:00 pm"
        new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("hh:mm a")
            .toFormatter(Locale.ENGLISH), // "07:00 PM", "07:00 pm"
        new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("h:mma")
            .toFormatter(Locale.ENGLISH), // "7:00PM", "7:00pm"
        new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("hh:mma")
            .toFormatter(Locale.ENGLISH)  // "07:00PM", "07:00pm"
    };

    @Override
    public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText().trim();
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalTime.parse(text, formatter);
            } catch (Exception ignored) {
                // Ignore exception to try parsing with the next formatter pattern
            }
        }
        throw new IOException("Unable to parse LocalTime: " + text);
    }
}
