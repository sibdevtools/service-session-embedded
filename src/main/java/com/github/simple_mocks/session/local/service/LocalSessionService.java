package com.github.simple_mocks.session.local.service;

import com.github.simple_mocks.error_service.exception.ServiceException;
import com.github.simple_mocks.session.api.ModificationQuery;
import com.github.simple_mocks.session.api.SessionErrors;
import com.github.simple_mocks.session.api.SessionId;
import com.github.simple_mocks.session.api.SessionService;
import com.github.simple_mocks.session.api.dto.SessionOwnerType;
import com.github.simple_mocks.session.api.dto.action.AddAction;
import com.github.simple_mocks.session.api.dto.action.DeleteAction;
import com.github.simple_mocks.session.api.dto.action.SetAction;
import com.github.simple_mocks.session.local.dto.LocalSession;
import com.github.simple_mocks.session.local.entity.*;
import com.github.simple_mocks.session.local.exception.SessionNotFoundException;
import com.github.simple_mocks.session.local.repository.PermissionEntityRepository;
import com.github.simple_mocks.session.local.repository.SessionEntityRepository;
import com.github.simple_mocks.session.local.repository.SessionSnapshotEntityRepository;
import jakarta.annotation.Nonnull;
import jakarta.transaction.Transactional;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public class LocalSessionService implements SessionService {
    private static final SnapshotCodecType DEFAULT_CODEC_TYPE = SnapshotCodecType.JSON;

    private final SessionEntityRepository sessionEntityRepository;
    private final SessionSnapshotEntityRepository sessionSnapshotEntityRepository;
    private final PermissionEntityRepository permissionEntityRepository;
    private final Map<SnapshotCodecType, SnapshotCodec> snapshotCodecs;

    /**
     * Construct local service
     *
     * @param sessionEntityRepository         session entity repository
     * @param sessionSnapshotEntityRepository session snapshot entity repository
     * @param permissionEntityRepository      permission entity repository
     * @param snapshotCodecs                  supported snapshot codecs
     */
    public LocalSessionService(SessionEntityRepository sessionEntityRepository,
                               SessionSnapshotEntityRepository sessionSnapshotEntityRepository,
                               PermissionEntityRepository permissionEntityRepository,
                               List<SnapshotCodec> snapshotCodecs) {
        this.sessionEntityRepository = sessionEntityRepository;
        this.sessionSnapshotEntityRepository = sessionSnapshotEntityRepository;
        this.permissionEntityRepository = permissionEntityRepository;
        this.snapshotCodecs = snapshotCodecs.stream()
                .collect(Collectors.toMap(SnapshotCodec::getType, Function.identity()));
    }

    @Override
    @Transactional
    public LocalSession get(@Nonnull SessionId sessionId) {
        var sessionSnapshot = getSessionSnapshotEntity(sessionId);

        return buildLocalSession(sessionId, sessionSnapshot);
    }

    @Override
    @Transactional
    public LocalSession get(@Nonnull String uid) {
        var sessionSnapshot = sessionSnapshotEntityRepository.findTopByEntityIdUidOrderByEntityIdVersionAsc(uid)
                .orElseThrow(() -> new SessionNotFoundException(uid));

        var snapshotId = sessionSnapshot.getEntityId();
        var sessionId = SessionId.of(snapshotId.getUid(), snapshotId.getVersion());

        return buildLocalSession(sessionId, sessionSnapshot);
    }

    @Override
    @Transactional
    public Set<String> getAttributeNames(@Nonnull SessionId sessionId,
                                         @Nonnull String section) {
        var sessionSnapshot = getSessionSnapshotEntity(sessionId);

        var attributes = getSnapshotAttributes(sessionSnapshot);

        return Optional.ofNullable(attributes)
                .map(it -> it.get(section))
                .map(Map::keySet)
                .orElseGet(Collections::emptySet);
    }

    @Override
    @Transactional
    public <T extends Serializable> T getAttribute(@Nonnull SessionId sessionId,
                                                   @Nonnull String section,
                                                   @Nonnull String attribute) {
        var sessionSnapshot = getSessionSnapshotEntity(sessionId);

        return Optional.ofNullable(getSnapshotAttributes(sessionSnapshot))
                .map(it -> it.get(section))
                .map(it -> (T) it.get(attribute))
                .orElse(null);
    }

    private Map<String, Map<String, Serializable>> getSnapshotAttributes(SessionSnapshotEntity sessionSnapshot) {
        var snapshotCodec = getSnapshotCodec(sessionSnapshot.getSnapshotType());
        return snapshotCodec.deserialize(sessionSnapshot.getSnapshot());
    }

    @Override
    @Transactional
    public SessionId create(@Nonnull Map<String, Map<String, Serializable>> sections,
                            @Nonnull SessionOwnerType ownerType,
                            @Nonnull String ownerId,
                            @Nonnull List<String> permissions) {
        var snapshotCodec = getSnapshotCodec(DEFAULT_CODEC_TYPE);
        var snapshot = snapshotCodec.serialize(sections);

        var sessionUid = UUID.randomUUID().toString();

        var permissionsEntities = permissions.stream()
                .map(this::getOrCreatePermission)
                .toList();

        var sessionEntity = SessionEntity.builder()
                .uid(sessionUid)
                .ownerType(ownerType)
                .ownerId(ownerId)
                .createdAt(ZonedDateTime.now())
                .permissions(permissionsEntities)
                .build();

        sessionEntity = sessionEntityRepository.save(sessionEntity);

        var sessionSnapshot = SessionSnapshotEntity.builder()
                .entityId(
                        SessionSnapshotEntityId.builder()
                                .uid(sessionUid)
                                .version(0)
                                .build()
                )
                .session(sessionEntity)
                .snapshot(snapshot)
                .snapshotType(DEFAULT_CODEC_TYPE)
                .createdAt(ZonedDateTime.now())
                .build();

        sessionSnapshotEntityRepository.save(sessionSnapshot);

        return SessionId.of(sessionUid, 0);
    }

    private SnapshotCodec getSnapshotCodec(SnapshotCodecType snapshotCodecType) {
        var snapshotCodec = snapshotCodecs.get(snapshotCodecType);
        if (snapshotCodec == null) {
            throw new ServiceException(
                    SessionErrors.UNEXPECTED_ERROR,
                    "Snapshot code type %s not supported".formatted(snapshotCodecType)
            );
        }
        return snapshotCodec;
    }

    private PermissionEntity getOrCreatePermission(String code) {
        return permissionEntityRepository.findByCodeIgnoreCase(code)
                .orElseGet(() -> {
                            var permissionEntity = PermissionEntity.builder()
                                    .code(code)
                                    .build();
                            return permissionEntityRepository.save(permissionEntity);
                        }
                );
    }

    @Override
    @Transactional
    public SessionId update(@Nonnull SessionId sessionId,
                            @Nonnull ModificationQuery modificationQuery) {
        var sessionEntity = getSessionSnapshotEntity(sessionId);
        var session = buildLocalSession(sessionId, sessionEntity);

        var attributes = updateAttributes(modificationQuery, session);

        var snapshotCodec = getSnapshotCodec(DEFAULT_CODEC_TYPE);
        var snapshot = snapshotCodec.serialize(attributes);

        var uid = sessionId.getUID();
        var newVersion = sessionId.getVersion() + 1;
        var newSnapshotId = SessionId.of(uid, newVersion);

        var sessionSnapshot = SessionSnapshotEntity.builder()
                .entityId(
                        SessionSnapshotEntityId.builder()
                                .uid(uid)
                                .version(newVersion)
                                .build()
                )
                .session(sessionEntity.getSession())
                .snapshot(snapshot)
                .snapshotType(DEFAULT_CODEC_TYPE)
                .createdAt(ZonedDateTime.now())
                .build();

        sessionSnapshotEntityRepository.save(sessionSnapshot);

        return newSnapshotId;
    }

    private static HashMap<String, Map<String, Serializable>> updateAttributes(ModificationQuery modificationQuery,
                                                                               LocalSession session) {
        var attributes = makeMutableAttributes(session);

        var actions = modificationQuery.actions();
        for (var action : actions) {
            var section = action.getSection();
            var attributeName = action.getAttributeName();

            var sectionAttributes = attributes.computeIfAbsent(section, k -> new HashMap<>());
            if (action instanceof AddAction addAction) {
                var attributeValue = addAction.getAttributeValue();

                if (sectionAttributes.put(attributeName, attributeValue) != null) {
                    throw new ServiceException(
                            403,
                            SessionErrors.ALREADY_EXISTS,
                            "Attribute %s already exists in section %s".formatted(attributeName, section)
                    );
                }
            } else if (action instanceof SetAction setAction) {
                if (sectionAttributes.containsKey(attributeName)) {
                    var addIfNotExists = setAction.isAddIfNotExists();
                    if (!addIfNotExists) {
                        throw new ServiceException(
                                403,
                                SessionErrors.NOT_EXISTS,
                                "Attribute '%s' not found in section %s".formatted(attributeName, section)
                        );
                    }
                }
                var attributeValue = setAction.getAttributeValue();
                sectionAttributes.put(attributeName, attributeValue);
            } else if (action instanceof DeleteAction && sectionAttributes.remove(attributeName) == null) {
                throw new ServiceException(
                        403,
                        SessionErrors.NOT_EXISTS,
                        "Attribute '%s' not found in section %s".formatted(attributeName, section)
                );
            }
        }
        return attributes;
    }

    private static HashMap<String, Map<String, Serializable>> makeMutableAttributes(LocalSession session) {
        var sourceAttributes = session.getAttributes();
        var attributes = new HashMap<String, Map<String, Serializable>>(sourceAttributes.size(), 1);

        for (var entry : sourceAttributes.entrySet()) {
            var sectionId = entry.getKey();
            var sectionData = entry.getValue();
            attributes.put(sectionId, new HashMap<>(sectionData));
        }
        return attributes;
    }

    private SessionSnapshotEntity getSessionSnapshotEntity(SessionId sessionId) {
        var uid = sessionId.getUID();
        var version = sessionId.getVersion();
        return sessionSnapshotEntityRepository.findByEntityIdUidAndEntityIdVersion(uid, version)
                .orElseThrow(() -> new SessionNotFoundException(uid));
    }

    private LocalSession buildLocalSession(SessionId sessionId, SessionSnapshotEntity sessionSnapshot) {
        var attributes = getSnapshotAttributes(sessionSnapshot);

        var session = sessionSnapshot.getSession();
        var permissions = session.getPermissions()
                .stream()
                .map(PermissionEntity::getCode)
                .collect(Collectors.toSet());

        return LocalSession.builder()
                .id(sessionId)
                .ownerType(session.getOwnerType())
                .ownerId(session.getOwnerId())
                .attributes(attributes)
                .permissions(permissions)
                .build();
    }
}
