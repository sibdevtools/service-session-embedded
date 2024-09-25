package com.github.sibdevtools.session.embedded.repository;

import com.github.sibdevtools.session.embedded.entity.SessionSnapshotEntity;
import com.github.sibdevtools.session.embedded.entity.SessionSnapshotEntityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public interface SessionSnapshotEntityRepository extends JpaRepository<SessionSnapshotEntity, SessionSnapshotEntityId> {

    /**
     * Looking for session snapshot by session id uid and version.
     *
     * @param uid     session uid
     * @param version snapshot version
     * @return found session snapshot or empty
     */
    Optional<SessionSnapshotEntity> findByEntityIdUidAndEntityIdVersion(String uid, long version);

    /**
     * Find last session snapshot by entity id uid.
     *
     * @param uid session uid
     * @return found session snapshot or empty
     */
    Optional<SessionSnapshotEntity> findTopByEntityIdUidOrderByEntityIdVersionAsc(String uid);

}
