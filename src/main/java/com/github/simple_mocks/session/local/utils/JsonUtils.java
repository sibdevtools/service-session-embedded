package com.github.simple_mocks.session.local.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;

/**
 * @author sibmaks
 * @since 0.0.1
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Convert object value into json string
     *
     * @param object object
     * @return json object representation
     * @throws JsonProcessingException conversion exception
     */
    public static String toString(Object object) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(object);
    }

    /**
     * Read json value from a byte array and convert it into a specific type
     *
     * @param buffer byte array
     * @param klass  destination type
     * @param <T>    type of destination
     * @return parsed object
     * @throws IOException read exception
     */
    public static <T> T read(byte[] buffer, Class<T> klass) throws IOException {
        return OBJECT_MAPPER.readValue(buffer, klass);
    }
}
