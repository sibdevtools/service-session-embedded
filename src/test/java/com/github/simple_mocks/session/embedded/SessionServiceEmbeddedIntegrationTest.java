package com.github.simple_mocks.session.embedded;

import com.github.simple_mocks.session.api.ModificationQueryBuilder;
import com.github.simple_mocks.session.api.dto.SessionId;
import com.github.simple_mocks.session.api.dto.SessionOwnerType;
import com.github.simple_mocks.session.api.rq.CreateSessionRq;
import com.github.simple_mocks.session.api.rq.GetSessionAttributeNamesRq;
import com.github.simple_mocks.session.api.rq.GetSessionAttributeRq;
import com.github.simple_mocks.session.api.rq.UpdateSessionRq;
import com.github.simple_mocks.session.api.service.SessionService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author sibmaks
 * @since 0.0.3
 */
@ActiveProfiles("startup-test")
@EnableSessionServiceEmbedded
@SpringBootApplication
class SessionServiceEmbeddedIntegrationTest {

    @ParameterizedTest
    @EnumSource(SessionOwnerType.class)
    void testCreateSessionAndGet(SessionOwnerType ownerType) {
        try (var context = SpringApplication.run(SessionServiceEmbeddedIntegrationTest.class)) {
            assertNotNull(context);

            var sessionService = context.getBean(SessionService.class);

            var ownerUID = UUID.randomUUID().toString();
            var permission = UUID.randomUUID().toString();
            var sectionId = UUID.randomUUID().toString();

            var attributeKey = UUID.randomUUID().toString();
            var attributeValue = UUID.randomUUID().hashCode();

            var sessionId = sessionService.create(
                    CreateSessionRq.builder()
                            .ownerId(ownerUID)
                            .ownerType(ownerType)
                            .permissions(
                                    List.of(
                                            permission
                                    )
                            )
                            .sections(
                                    Map.of(
                                            sectionId, Map.of(
                                                    attributeKey, attributeValue
                                            )
                                    )
                            )
                            .build()
            );

            assertNotNull(sessionId);

            var session = sessionService.get(sessionId);
            assertNotNull(session);

            assertEquals(ownerUID, session.getOwnerId());
            assertEquals(ownerType, session.getOwnerType());

            var permissions = session.getPermissions();
            assertNotNull(permissions);

            assertEquals(Set.of(permission), permissions);

            var sections = session.getSections();
            assertNotNull(sections);

            assertTrue(sections.contains(sectionId));

            var attributeNames = sessionService.getAttributeNames(
                    GetSessionAttributeNamesRq.builder()
                            .sessionId(sessionId)
                            .section(sectionId)
                            .build()
            );

            assertEquals(Set.of(attributeKey), attributeNames);

            assertAttribute(sessionService, sessionId, sectionId, attributeKey, attributeValue);
        }
    }

    @ParameterizedTest
    @EnumSource(SessionOwnerType.class)
    void testCreateSessionAndUpdateAttributes(SessionOwnerType ownerType) {
        try (var context = SpringApplication.run(SessionServiceEmbeddedIntegrationTest.class)) {
            assertNotNull(context);

            var sessionService = context.getBean(SessionService.class);

            var ownerUID = UUID.randomUUID().toString();
            var permission = UUID.randomUUID().toString();
            var sectionId = UUID.randomUUID().toString();

            var attributeKey = "not_change:" + UUID.randomUUID();
            var attributeValue = UUID.randomUUID().hashCode();

            var toChangeAttributeKey = "change:" + UUID.randomUUID();
            var toChangeAttributeValue = UUID.randomUUID().toString();

            var toRemoveAttributeKey = "remove:" + UUID.randomUUID();

            var sessionId = sessionService.create(
                    CreateSessionRq.builder()
                            .ownerId(ownerUID)
                            .ownerType(ownerType)
                            .permissions(
                                    List.of(
                                            permission
                                    )
                            )
                            .sections(
                                    Map.of(
                                            sectionId,
                                            Map.of(
                                                    attributeKey, attributeValue,
                                                    toChangeAttributeKey, toChangeAttributeValue,
                                                    toRemoveAttributeKey, UUID.randomUUID().toString()
                                            )
                                    )
                            )
                            .build()
            );

            assertNotNull(sessionId);

            var newAttributeKey = "add:" + UUID.randomUUID();
            var newAttributeValue = UUID.randomUUID().toString();
            var toChangeAttributeNewValue = UUID.randomUUID().toString();

            var updatedSessionId = sessionService.update(
                    UpdateSessionRq.builder()
                            .sessionId(sessionId)
                            .modificationQuery(
                                    ModificationQueryBuilder.builder()
                                            .create(sectionId, newAttributeKey, newAttributeValue)
                                            .change(sectionId, toChangeAttributeKey, toChangeAttributeNewValue)
                                            .remove(sectionId, toRemoveAttributeKey)
                                            .build()
                            )
                            .build()
            );


            var session = sessionService.get(updatedSessionId);
            assertNotNull(session);

            var sections = session.getSections();
            assertNotNull(sections);

            assertTrue(sections.contains(sectionId));

            var attributeNames = sessionService.getAttributeNames(
                    GetSessionAttributeNamesRq.builder()
                            .sessionId(updatedSessionId)
                            .section(sectionId)
                            .build()
            );

            assertEquals(Set.of(attributeKey, newAttributeKey, toChangeAttributeKey), attributeNames);

            assertAttribute(sessionService, updatedSessionId, sectionId, attributeKey, attributeValue);
            assertAttribute(sessionService, updatedSessionId, sectionId, newAttributeKey, newAttributeValue);
            assertAttribute(sessionService, updatedSessionId, sectionId, toChangeAttributeKey, toChangeAttributeNewValue);

            assertAttribute(sessionService, sessionId, sectionId, attributeKey, attributeValue);
            assertAttribute(sessionService, sessionId, sectionId, newAttributeKey, null);
            assertAttribute(sessionService, sessionId, sectionId, toChangeAttributeKey, toChangeAttributeValue);
        }
    }

    private static void assertAttribute(SessionService sessionService,
                                        SessionId sessionId,
                                        String sectionId,
                                        String attributeKey,
                                        Object excepted) {
        var actual = sessionService.getAttribute(
                GetSessionAttributeRq.builder()
                        .sessionId(sessionId)
                        .section(sectionId)
                        .attribute(attributeKey)
                        .build()
        );
        assertEquals(excepted, actual);
    }

}