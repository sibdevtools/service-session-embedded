package com.github.simplemocks.session.embedded.repository;

import com.github.simplemocks.session.embedded.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public interface SessionEntityRepository extends JpaRepository<SessionEntity, String> {

}
