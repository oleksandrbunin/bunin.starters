package org.ob.starters.tenancystarter.exceptions;

import javax.persistence.LockTimeoutException;

public class LockReleaseException extends LockTimeoutException {
    public LockReleaseException(String message) {
        super(message);
    }

    public LockReleaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
