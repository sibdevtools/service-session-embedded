package com.github.simplemocks.session.embedded.conf;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.github.simplemocks.error_service.mutable.api.source.ErrorLocalizationsJsonSource;
import com.github.simplemocks.session.embedded.repository.PermissionEntityRepository;
import com.github.simplemocks.session.embedded.repository.SessionEntityRepository;
import com.github.simplemocks.session.embedded.repository.SessionSnapshotEntityRepository;
import com.github.simplemocks.session.embedded.service.SessionServiceEmbedded;
import com.github.simplemocks.session.embedded.service.SnapshotCodec;
import com.github.simplemocks.session.embedded.service.impl.SnapshotJsonCodec;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;
import java.util.List;

/**
 * @author sibmaks
 * @since 0.0.1
 */
@ErrorLocalizationsJsonSource(
        systemCode = "SESSION_SERVICE",
        iso3Code = "eng",
        path = "classpath:/embedded/session/content/errors/eng.json"
)
@ErrorLocalizationsJsonSource(
        systemCode = "SESSION_SERVICE",
        iso3Code = "rus",
        path = "classpath:/embedded/session/content/errors/rus.json"
)
@PropertySource("classpath:embedded-session-application.properties")
public final class SessionServiceEmbeddedConfig {

    @Bean
    public SessionServiceEmbedded sessionServiceEmbedded(
            SessionEntityRepository sessionEntityRepository,
            SessionSnapshotEntityRepository sessionSnapshotEntityRepository,
            PermissionEntityRepository permissionEntityRepository,
            List<SnapshotCodec> snapshotCodecs) {
        return new SessionServiceEmbedded(
                sessionEntityRepository,
                sessionSnapshotEntityRepository,
                permissionEntityRepository,
                snapshotCodecs
        );
    }

    @Bean("sessionServiceObjectMapper")
    public ObjectMapper sessionServiceObjectMapper() {
        return JsonMapper.builder()
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .addModule(new ParameterNamesModule())
                .addModule(new Jdk8Module())
                .addModule(new JavaTimeModule())
                .build();
    }

    @Bean
    public SnapshotJsonCodec snapshotJsonCodec(
            @Qualifier("sessionServiceObjectMapper")
            ObjectMapper objectMapper
    ) {
        return new SnapshotJsonCodec(objectMapper);
    }

    @Bean
    @ConfigurationProperties("spring.flyway.embedded-session")
    public ClassicConfiguration sessionEmbeddedFlywayConfiguration(DataSource dataSource) {
        var classicConfiguration = new ClassicConfiguration();
        classicConfiguration.setDataSource(dataSource);
        return classicConfiguration;
    }

    @Bean
    public Flyway sessionEmbeddedFlyway(
            @Qualifier("sessionEmbeddedFlywayConfiguration")
            ClassicConfiguration configuration
    ) {
        var flyway = new Flyway(configuration);
        flyway.migrate();
        return flyway;
    }
}
