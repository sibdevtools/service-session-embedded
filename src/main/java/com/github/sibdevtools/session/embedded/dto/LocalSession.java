package com.github.sibdevtools.session.embedded.dto;

import com.github.sibdevtools.session.api.dto.Session;
import com.github.sibdevtools.session.api.dto.SessionId;
import com.github.sibdevtools.session.api.dto.SessionOwnerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author sibmaks
 * @since 0.0.1
 */
@Getter
@Builder
@ToString
@AllArgsConstructor
public class LocalSession implements Session {
    private final SessionId id;
    private final SessionOwnerType ownerType;
    private final String ownerId;
    private final Map<String, Map<String, Serializable>> attributes;
    private final Set<String> permissions;

    @Override
    public Set<String> getSections() {
        return Collections.unmodifiableSet(attributes.keySet());
    }
}
