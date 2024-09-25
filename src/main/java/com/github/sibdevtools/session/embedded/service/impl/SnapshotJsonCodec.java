package com.github.sibdevtools.session.embedded.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sibdevtools.session.embedded.entity.SnapshotCodecType;
import com.github.sibdevtools.session.embedded.exception.UnexpectedErrorException;
import com.github.sibdevtools.session.embedded.service.SnapshotCodec;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author sibmaks
 * @since 0.0.1
 */
@AllArgsConstructor
public class SnapshotJsonCodec implements SnapshotCodec {
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Map<String, Serializable>> deserialize(byte[] snapshot) {
        try {
            return objectMapper.readValue(snapshot, Map.class);
        } catch (IOException e) {
            throw new UnexpectedErrorException("Can't deserialize snapshot", e);
        }
    }

    @Override
    public byte[] serialize(Map<String, Map<String, Serializable>> snapshot) {
        try {
            var json = objectMapper.writeValueAsString(snapshot);
            return json.getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            throw new UnexpectedErrorException("Can't serialize snapshot", e);
        }
    }

    @Override
    public SnapshotCodecType getType() {
        return SnapshotCodecType.JSON;
    }
}
