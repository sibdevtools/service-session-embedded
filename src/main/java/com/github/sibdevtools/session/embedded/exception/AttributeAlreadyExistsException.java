package com.github.sibdevtools.session.embedded.exception;

import com.github.sibdevtools.error.exception.ServiceException;
import com.github.sibdevtools.session.embedded.constant.Constants;

/**
 * @author sibmaks
 * @since 0.0.3
 */
public class AttributeAlreadyExistsException extends ServiceException {

    /**
     * Construct attribute already exists exception.
     *
     * @param systemMessage system message
     */
    public AttributeAlreadyExistsException(String systemMessage) {
        super(Constants.ERROR_SOURCE, "ATTRIBUTE_ALREADY_EXISTS", systemMessage);
    }

}
