package com.github.simple_mocks.session.embedded.exception;

import com.github.simple_mocks.error_service.exception.ServiceException;
import com.github.simple_mocks.session.embedded.constant.Constants;

/**
 * @author sibmaks
 * @since 0.0.3
 */
public class NotExistsException extends ServiceException {

    /**
     * Construct unexpected error exception.
     *
     * @param systemMessage system message
     */
    public NotExistsException(String systemMessage) {
        super(Constants.ERROR_SOURCE, "NOT_EXISTS", systemMessage);
    }

}
