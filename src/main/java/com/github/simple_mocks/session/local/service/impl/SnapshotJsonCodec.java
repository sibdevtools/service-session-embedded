package com.github.simple_mocks.session.local.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.simple_mocks.error_service.exception.ServiceException;
import com.github.simple_mocks.session.api.SessionErrors;
import com.github.simple_mocks.session.local.entity.SnapshotCodecType;
import com.github.simple_mocks.session.local.service.SnapshotCodec;
import com.github.simple_mocks.session.local.utils.JsonUtils;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public class SnapshotJsonCodec implements SnapshotCodec {

    @Override
    public Map<String, Map<String, Serializable>> deserialize(byte[] snapshot) {
        try {
            return JsonUtils.read(snapshot, Map.class);
        } catch (IOException e) {
            throw new ServiceException(SessionErrors.UNEXPECTED_ERROR, "Can't deserialize snapshot", e);
        }
    }

    @Override
    public byte[] serialize(Map<String, Map<String, Serializable>> snapshot) {
        try {
            var json = JsonUtils.toString(snapshot);
            return json.getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            throw new ServiceException(SessionErrors.UNEXPECTED_ERROR, "Can't serialize snapshot", e);
        }
    }

    @Override
    public SnapshotCodecType getType() {
        return SnapshotCodecType.JSON;
    }
}
