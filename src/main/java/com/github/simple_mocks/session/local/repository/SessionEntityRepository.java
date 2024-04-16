package com.github.simple_mocks.session.local.repository;

import com.github.simple_mocks.session.local.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public interface SessionEntityRepository extends JpaRepository<SessionEntity, String> {

}
