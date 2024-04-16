package com.github.simple_mocks.session.local.conf;

import com.github.simple_mocks.session.local.EnableLocalSessionService;
import com.github.simple_mocks.session.local.repository.PermissionEntityRepository;
import com.github.simple_mocks.session.local.repository.SessionEntityRepository;
import com.github.simple_mocks.session.local.repository.SessionSnapshotEntityRepository;
import com.github.simple_mocks.session.local.service.LocalSessionService;
import com.github.simple_mocks.session.local.service.SnapshotCodec;
import com.github.simple_mocks.session.local.service.impl.SnapshotJsonCodec;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author sibmaks
 * @since 0.0.1
 */
@PropertySource("classpath:local-session-application.properties")
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackageClasses = EnableLocalSessionService.class,
        entityManagerFactoryRef = "localSessionEntityManagerFactory",
        transactionManagerRef = "localSessionTransactionManager"
)
public final class LocalSessionServiceConfig {

    @Bean
    public LocalSessionService localSessionService(SessionEntityRepository sessionEntityRepository,
                                                   SessionSnapshotEntityRepository sessionSnapshotEntityRepository,
                                                   PermissionEntityRepository permissionEntityRepository,
                                                   List<SnapshotCodec> snapshotCodecs) {
        return new LocalSessionService(
                sessionEntityRepository,
                sessionSnapshotEntityRepository,
                permissionEntityRepository,
                snapshotCodecs
        );
    }

    @Bean
    public SnapshotJsonCodec snapshotJsonCodec() {
        return new SnapshotJsonCodec();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.local-session")
    public DataSourceProperties localSessionDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource localSessionDataSource(
            @Qualifier("localSessionDataSourceProperties") DataSourceProperties dataSourceProperties) {
        return dataSourceProperties
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    @ConfigurationProperties("spring.jpa.local-session.properties")
    public Map<String, String> localSessionJpaProperties() {
        return new HashMap<>();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean localSessionEntityManagerFactory(
            @Qualifier("localSessionDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder managerFactoryBuilder,
            @Qualifier("localSessionJpaProperties") Map<String, String> localSessionJpaProperties) {
        return managerFactoryBuilder
                .dataSource(dataSource)
                .packages(EnableLocalSessionService.class)
                .properties(localSessionJpaProperties)
                .build();
    }

    @Bean
    public PlatformTransactionManager localSessionTransactionManager(
            @Qualifier("localSessionEntityManagerFactory") LocalContainerEntityManagerFactoryBean managerFactoryBean) {
        var entityManagerFactory = managerFactoryBean.getObject();
        Objects.requireNonNull(entityManagerFactory);
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    @ConfigurationProperties("spring.flyway.local-session")
    public ClassicConfiguration localSessionFlywayConfiguration(@Qualifier("localSessionDataSource") DataSource dataSource) {
        var classicConfiguration = new ClassicConfiguration();
        classicConfiguration.setDataSource(dataSource);
        return classicConfiguration;
    }

    @Bean
    public Flyway localSessionFlyway(@Qualifier("localSessionFlywayConfiguration") ClassicConfiguration configuration) {
        var flyway = new Flyway(configuration);
        flyway.migrate();
        return flyway;
    }
}
