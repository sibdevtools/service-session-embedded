package com.github.sibdevtools.session.embedded.exception;

import com.github.sibdevtools.error.exception.ServiceException;
import com.github.sibdevtools.session.embedded.constant.Constants;

/**
 * @author sibmaks
 * @since 0.0.3
 */
public class AttributeNotExistsException extends ServiceException {

    /**
     * Construct unexpected error exception.
     *
     * @param systemMessage system message
     */
    public AttributeNotExistsException(String systemMessage) {
        super(Constants.ERROR_SOURCE, "ATTRIBUTE_NOT_EXISTS", systemMessage);
    }

}
