package com.github.simplemocks.session.embedded.service;

import com.github.simplemocks.session.embedded.entity.SnapshotCodecType;

import java.io.Serializable;
import java.util.Map;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public interface SnapshotCodec {

    /**
     * Method should deserialize session snapshot content into format: <code>section: {attr_key: attr_val}</code>
     *
     * @param snapshot source snapshot
     * @return deserialized session content
     */
    Map<String, Map<String, Serializable>> deserialize(byte[] snapshot);

    /**
     * Method should serialize session snapshot content into a byte array
     *
     * @param snapshot source snapshot
     * @return deserialized session content
     */
   byte[] serialize(Map<String, Map<String, Serializable>> snapshot);

    /**
     * Get a codec type
     *
     * @return codec type
     */
    SnapshotCodecType getType();
}
