package com.github.sibdevtools.session.embedded.constant;

import com.github.sibdevtools.error.api.dto.ErrorSourceId;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author sibmaks
 * @since 0.0.3
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    public static final ErrorSourceId ERROR_SOURCE = new ErrorSourceId("SESSION_SERVICE");

}
