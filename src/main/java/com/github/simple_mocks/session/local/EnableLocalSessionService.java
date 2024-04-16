package com.github.simple_mocks.session.local;

import com.github.simple_mocks.session.local.conf.LocalSessionServiceConfig;
import com.github.simple_mocks.session.local.service.LocalSessionService;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enabler for local implementation of storage service.
 *
 * @author sibmaks
 * @since 0.0.1
 * @see com.github.simple_mocks.session.api.SessionService
 * @see LocalSessionService
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(LocalSessionServiceConfig.class)
public @interface EnableLocalSessionService {
}
