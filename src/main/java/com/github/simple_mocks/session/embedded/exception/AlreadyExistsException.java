package com.github.simple_mocks.session.embedded.exception;

import com.github.simple_mocks.error_service.exception.ServiceException;
import com.github.simple_mocks.session.embedded.constant.Constants;

/**
 * @author sibmaks
 * @since 0.0.3
 */
public class AlreadyExistsException extends ServiceException {

    /**
     * Construct unexpected error exception.
     *
     * @param systemMessage system message
     */
    public AlreadyExistsException(String systemMessage) {
        super(Constants.ERROR_SOURCE, "ALREADY_EXISTS", systemMessage);
    }

}
