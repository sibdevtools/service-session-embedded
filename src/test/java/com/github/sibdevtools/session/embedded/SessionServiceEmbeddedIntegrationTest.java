package com.github.sibdevtools.session.embedded;

import com.github.sibdevtools.session.api.ModificationQueryBuilder;
import com.github.sibdevtools.session.api.dto.SessionId;
import com.github.sibdevtools.session.api.dto.SessionOwnerType;
import com.github.sibdevtools.session.api.rq.CreateSessionRq;
import com.github.sibdevtools.session.api.rq.GetSessionAttributeNamesRq;
import com.github.sibdevtools.session.api.rq.GetSessionAttributeRq;
import com.github.sibdevtools.session.api.rq.UpdateSessionRq;
import com.github.sibdevtools.session.api.service.SessionService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
@SpringBootTest
class SessionServiceEmbeddedIntegrationTest {
    @Autowired
    private SessionService sessionService;

    @ParameterizedTest
    @EnumSource(SessionOwnerType.class)
    void testCreateSessionAndGet(SessionOwnerType ownerType) {

        var ownerUID = UUID.randomUUID().toString();
        var permission = UUID.randomUUID().toString();
        var sectionId = UUID.randomUUID().toString();

        var attributeKey = UUID.randomUUID().toString();
        var attributeValue = UUID.randomUUID().hashCode();

        var createSessionRq = sessionService.create(
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

        assertNotNull(createSessionRq);

        var sessionId = createSessionRq.getBody();
        assertNotNull(sessionId);

        var getSessionRs = sessionService.get(sessionId);
        assertNotNull(getSessionRs);

        var session = getSessionRs.getBody();

        assertEquals(ownerUID, session.getOwnerId());
        assertEquals(ownerType, session.getOwnerType());

        var permissions = session.getPermissions();
        assertNotNull(permissions);

        assertEquals(Set.of(permission), permissions);

        var sections = session.getSections();
        assertNotNull(sections);

        assertTrue(sections.contains(sectionId));

        var attributeNamesRs = sessionService.getAttributeNames(
                GetSessionAttributeNamesRq.builder()
                        .sessionId(sessionId)
                        .section(sectionId)
                        .build()
        );
        assertNotNull(attributeNamesRs);

        var attributeNames = attributeNamesRs.getBody();

        assertEquals(Set.of(attributeKey), attributeNames);

        assertAttribute(sessionService, sessionId, sectionId, attributeKey, attributeValue);
    }


    @ParameterizedTest
    @EnumSource(SessionOwnerType.class)
    void testCreateSessionAndUpdateAttributes(SessionOwnerType ownerType) {

        var ownerUID = UUID.randomUUID().toString();
        var permission = UUID.randomUUID().toString();
        var sectionId = UUID.randomUUID().toString();

        var attributeKey = "not_change:" + UUID.randomUUID();
        var attributeValue = UUID.randomUUID().hashCode();

        var toChangeAttributeKey = "change:" + UUID.randomUUID();
        var toChangeAttributeValue = UUID.randomUUID().toString();

        var toRemoveAttributeKey = "remove:" + UUID.randomUUID();

        var createSessionRs = sessionService.create(
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

        assertNotNull(createSessionRs);

        var sessionId = createSessionRs.getBody();

        var newAttributeKey = "add:" + UUID.randomUUID();
        var newAttributeValue = UUID.randomUUID().toString();
        var toChangeAttributeNewValue = UUID.randomUUID().toString();

        var updateSessionRs = sessionService.update(
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
        assertNotNull(updateSessionRs);

        var updatedSessionId = updateSessionRs.getBody();
        assertNotNull(updatedSessionId);

        var getSessionRs = sessionService.get(updatedSessionId);
        assertNotNull(getSessionRs);

        var session = getSessionRs.getBody();
        assertNotNull(session);

        var sections = session.getSections();
        assertNotNull(sections);

        assertTrue(sections.contains(sectionId));

        var attributeNamesRs = sessionService.getAttributeNames(
                GetSessionAttributeNamesRq.builder()
                        .sessionId(updatedSessionId)
                        .section(sectionId)
                        .build()
        );
        assertNotNull(attributeNamesRs);

        var attributeNames = attributeNamesRs.getBody();

        assertEquals(Set.of(attributeKey, newAttributeKey, toChangeAttributeKey), attributeNames);

        assertAttribute(sessionService, updatedSessionId, sectionId, attributeKey, attributeValue);
        assertAttribute(sessionService, updatedSessionId, sectionId, newAttributeKey, newAttributeValue);
        assertAttribute(sessionService, updatedSessionId, sectionId, toChangeAttributeKey, toChangeAttributeNewValue);

        assertAttribute(sessionService, sessionId, sectionId, attributeKey, attributeValue);
        assertAttribute(sessionService, sessionId, sectionId, newAttributeKey, null);
        assertAttribute(sessionService, sessionId, sectionId, toChangeAttributeKey, toChangeAttributeValue);
    }

    private static void assertAttribute(SessionService sessionService,
                                        SessionId sessionId,
                                        String sectionId,
                                        String attributeKey,
                                        Object excepted) {
        var attributeRs = sessionService.getAttribute(
                GetSessionAttributeRq.builder()
                        .sessionId(sessionId)
                        .section(sectionId)
                        .attribute(attributeKey)
                        .build()
        );
        assertNotNull(attributeRs);

        var actual = attributeRs.getBody();
        assertEquals(excepted, actual);
    }

}