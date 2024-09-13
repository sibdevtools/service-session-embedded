package com.github.simplemocks.session.embedded;

import com.github.simplemocks.session.embedded.conf.SessionServiceEmbeddedConfig;
import com.github.simplemocks.session.embedded.service.SessionServiceEmbedded;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enabler for embedded implementation of session service.
 *
 * @author sibmaks
 * @since 0.0.1
 * @see com.github.simplemocks.session.api.service.SessionService
 * @see SessionServiceEmbedded
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(SessionServiceEmbeddedConfig.class)
public @interface EnableSessionServiceEmbedded {
}
