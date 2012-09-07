package com.heroku.janvil;

/**
 * @author Ryan Brainard
 */
public class JanvilBuildException extends JanvilRuntimeException {

    private final int exitStatus;

    public JanvilBuildException(int exitStatus) {
        this.exitStatus = exitStatus;
    }

    public int getExitStatus() {
        return exitStatus;
    }
}
