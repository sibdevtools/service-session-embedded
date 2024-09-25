package com.github.sibdevtools.session.embedded.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

/**
 * @author sibmaks
 * @since 0.0.1
 */
@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class SessionSnapshotEntityId implements Serializable {
    /**
     * Session uid
     */
    @Column(name = "uid")
    private String uid;
    /**
     * Snapshot version
     */
    @Column(name = "version")
    private long version;
}
