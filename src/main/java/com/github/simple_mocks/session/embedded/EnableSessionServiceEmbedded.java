package com.github.simple_mocks.session.embedded;

import com.github.simple_mocks.session.embedded.conf.SessionServiceEmbeddedConfig;
import com.github.simple_mocks.session.embedded.service.SessionServiceEmbedded;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enabler for embedded implementation of session service.
 *
 * @author sibmaks
 * @since 0.0.1
 * @see com.github.simple_mocks.session.api.service.SessionService
 * @see SessionServiceEmbedded
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(SessionServiceEmbeddedConfig.class)
public @interface EnableSessionServiceEmbedded {
}
