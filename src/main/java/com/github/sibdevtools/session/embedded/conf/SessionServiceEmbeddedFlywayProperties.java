package com.github.sibdevtools.session.embedded.conf;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author sibmaks
 * @since 0.0.10
 */
@Setter
@Getter
@Configuration
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties("service.session.embedded.flyway")
public class SessionServiceEmbeddedFlywayProperties {
    private String encoding;
    private String[] locations;
    private String schema;
}
