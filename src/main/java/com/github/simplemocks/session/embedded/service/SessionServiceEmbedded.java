package com.github.simplemocks.session.embedded.service;

import com.github.simplemocks.session.api.dto.SessionId;
import com.github.simplemocks.session.api.dto.query.ModificationQuery;
import com.github.simplemocks.session.api.dto.query.action.Action;
import com.github.simplemocks.session.api.dto.query.action.AddAction;
import com.github.simplemocks.session.api.dto.query.action.DeleteAction;
import com.github.simplemocks.session.api.dto.query.action.SetAction;
import com.github.simplemocks.session.api.rq.CreateSessionRq;
import com.github.simplemocks.session.api.rq.GetSessionAttributeNamesRq;
import com.github.simplemocks.session.api.rq.GetSessionAttributeRq;
import com.github.simplemocks.session.api.rq.UpdateSessionRq;
import com.github.simplemocks.session.api.rs.*;
import com.github.simplemocks.session.api.service.SessionService;
import com.github.simplemocks.session.embedded.dto.LocalSession;
import com.github.simplemocks.session.embedded.entity.*;
import com.github.simplemocks.session.embedded.exception.AttributeAlreadyExistsException;
import com.github.simplemocks.session.embedded.exception.AttributeNotExistsException;
import com.github.simplemocks.session.embedded.exception.SessionNotFoundException;
import com.github.simplemocks.session.embedded.exception.UnexpectedErrorException;
import com.github.simplemocks.session.embedded.repository.PermissionEntityRepository;
import com.github.simplemocks.session.embedded.repository.SessionEntityRepository;
import com.github.simplemocks.session.embedded.repository.SessionSnapshotEntityRepository;
import jakarta.annotation.Nonnull;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public class SessionServiceEmbedded implements SessionService {
    private static final SnapshotCodecType DEFAULT_CODEC_TYPE = SnapshotCodecType.JSON;

    private final SessionEntityRepository sessionEntityRepository;
    private final SessionSnapshotEntityRepository sessionSnapshotEntityRepository;
    private final PermissionEntityRepository permissionEntityRepository;
    private final Map<SnapshotCodecType, SnapshotCodec> snapshotCodecs;

    /**
     * Construct embedded session service
     *
     * @param sessionEntityRepository         session entity repository
     * @param sessionSnapshotEntityRepository session snapshot entity repository
     * @param permissionEntityRepository      permission entity repository
     * @param snapshotCodecs                  supported snapshot codecs
     */
    public SessionServiceEmbedded(SessionEntityRepository sessionEntityRepository,
                                  SessionSnapshotEntityRepository sessionSnapshotEntityRepository,
                                  PermissionEntityRepository permissionEntityRepository,
                                  List<SnapshotCodec> snapshotCodecs) {
        this.sessionEntityRepository = sessionEntityRepository;
        this.sessionSnapshotEntityRepository = sessionSnapshotEntityRepository;
        this.permissionEntityRepository = permissionEntityRepository;
        this.snapshotCodecs = snapshotCodecs.stream()
                .collect(Collectors.toMap(SnapshotCodec::getType, Function.identity()));
    }

    @Nonnull
    @Override
    @Transactional
    public GetSessionRs get(@Nonnull SessionId sessionId) {
        var sessionSnapshot = getSessionSnapshotEntity(sessionId);

        var session = buildLocalSession(sessionId, sessionSnapshot);
        return new GetSessionRs(session);
    }

    @Nonnull
    @Override
    @Transactional
    public GetSessionRs get(@Nonnull String uid) {
        var sessionSnapshot = sessionSnapshotEntityRepository.findTopByEntityIdUidOrderByEntityIdVersionAsc(uid)
                .orElseThrow(() -> new SessionNotFoundException(uid));

        var snapshotId = sessionSnapshot.getEntityId();
        var sessionId = SessionId.of(snapshotId.getUid(), snapshotId.getVersion());

        var session = buildLocalSession(sessionId, sessionSnapshot);
        return new GetSessionRs(session);
    }

    @Nonnull
    @Override
    public GetAttributeNamesRs getAttributeNames(@Nonnull GetSessionAttributeNamesRq rq) {
        var sessionId = rq.sessionId();
        var section = rq.section();

        var sessionSnapshot = getSessionSnapshotEntity(sessionId);

        var attributes = getSnapshotAttributes(sessionSnapshot);

        var attributeNames = Optional.ofNullable(attributes)
                .map(it -> it.get(section))
                .map(Map::keySet)
                .orElseGet(Collections::emptySet);
        return new GetAttributeNamesRs(attributeNames);
    }

    @Nonnull
    @Override
    public <T extends Serializable> GetSessionAttributeRs<T> getAttribute(@Nonnull GetSessionAttributeRq rq) {
        var sessionSnapshot = getSessionSnapshotEntity(rq.sessionId());

        var attribute = Optional.ofNullable(getSnapshotAttributes(sessionSnapshot))
                .map(it -> it.get(rq.section()))
                .map(it -> (T) it.get(rq.attribute()))
                .orElse(null);
        return new GetSessionAttributeRs<>(attribute);
    }

    private Map<String, Map<String, Serializable>> getSnapshotAttributes(SessionSnapshotEntity sessionSnapshot) {
        var snapshotCodec = getSnapshotCodec(sessionSnapshot.getSnapshotType());
        return snapshotCodec.deserialize(sessionSnapshot.getSnapshot());
    }

    @Nonnull
    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public CreateSessionRs create(@Nonnull CreateSessionRq rq) {
        var snapshotCodec = getSnapshotCodec(DEFAULT_CODEC_TYPE);
        var snapshot = snapshotCodec.serialize(rq.sections());

        var sessionUid = UUID.randomUUID().toString();

        var permissionsEntities = rq.permissions()
                .stream()
                .map(this::getOrCreatePermission)
                .toList();

        var sessionEntity = SessionEntity.builder()
                .uid(sessionUid)
                .ownerType(rq.ownerType())
                .ownerId(rq.ownerId())
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

        return new CreateSessionRs(SessionId.of(sessionUid, 0));
    }

    private SnapshotCodec getSnapshotCodec(SnapshotCodecType snapshotCodecType) {
        var snapshotCodec = snapshotCodecs.get(snapshotCodecType);
        if (snapshotCodec == null) {
            throw new UnexpectedErrorException("Snapshot code type %s not supported".formatted(snapshotCodecType));
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

    @Nonnull
    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public UpdateSessionRs update(@Nonnull UpdateSessionRq rq) {
        var sessionId = rq.sessionId();

        var sessionEntity = getSessionSnapshotEntity(sessionId);
        var session = buildLocalSession(sessionId, sessionEntity);

        var modificationQuery = rq.modificationQuery();
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

        return new UpdateSessionRs(newSnapshotId);
    }

    private static HashMap<String, Map<String, Serializable>> updateAttributes(
            ModificationQuery modificationQuery,
            LocalSession session
    ) {
        var attributes = makeMutableAttributes(session);

        var actions = modificationQuery.actions();
        for (var action : actions) {
            var section = action.getSection();
            var attributeName = action.getAttributeName();

            var sectionAttributes = attributes.computeIfAbsent(section, k -> new HashMap<>());
            proceedAction(action, sectionAttributes, attributeName, section);
        }
        return attributes;
    }

    private static void proceedAction(Action action,
                                      Map<String, Serializable> sectionAttributes,
                                      String attributeName,
                                      String section) {
        switch (action) {
            case AddAction addAction -> {
                var attributeValue = addAction.getAttributeValue();

                if (sectionAttributes.put(attributeName, attributeValue) != null) {
                    throw new AttributeAlreadyExistsException(
                            "Attribute %s already exists in section %s".formatted(attributeName, section)
                    );
                }
            }
            case SetAction setAction -> {
                if (!sectionAttributes.containsKey(attributeName)) {
                    var addIfNotExists = setAction.isAddIfNotExists();
                    if (!addIfNotExists) {
                        throw new AttributeNotExistsException(
                                "Attribute '%s' not found in section %s".formatted(attributeName, section)
                        );
                    }
                }
                var attributeValue = setAction.getAttributeValue();
                sectionAttributes.put(attributeName, attributeValue);
            }
            case DeleteAction ignored -> {
                if (sectionAttributes.remove(attributeName) != null) {
                    return;
                }
                throw new AttributeNotExistsException(
                        "Attribute '%s' not found in section %s".formatted(attributeName, section)
                );
            }
            default -> throw new UnexpectedErrorException("Unsupported action '%s'".formatted(action));
        }
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
