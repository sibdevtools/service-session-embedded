package com.github.simple_mocks.session.local.exception;

import com.github.simple_mocks.error_service.exception.ServiceException;
import com.github.simple_mocks.session.api.SessionErrors;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public class SessionNotFoundException extends ServiceException {
    private static final String SESSION_NOT_EXISTS_TEMPLATE = "Session %s not exists";

    /**
     * Construct session not found exception.
     *
     * @param uid session uid
     */
    public SessionNotFoundException(String uid) {
        super(
                404,
                SessionErrors.NOT_EXISTS,
                SESSION_NOT_EXISTS_TEMPLATE.formatted(uid)
        );
    }
}
