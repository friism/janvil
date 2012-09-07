package com.heroku.janvil;

/**
 * @author Ryan Brainard
 */
public class JanvilRuntimeException extends RuntimeException {

    public JanvilRuntimeException() {
    }

    public JanvilRuntimeException(String message) {
        super(message);
    }

    public JanvilRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public JanvilRuntimeException(Throwable cause) {
        super(cause);
    }
}
