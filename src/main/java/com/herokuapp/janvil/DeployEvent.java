package com.herokuapp.janvil;

/**
* @author Ryan Brainard
*/
public enum DeployEvent {
    DEPLOY_START,
    DIFF_START,
    DIFF_END,
    UPLOAD_FILE_START,
    UPLOAD_FILE_END,
    UPLOADS_START,
    UPLOADS_END,
    BUILD_START,
    BUILD_END,
    BUILD_OUTPUT_LINE, RELEASE_START, RELEASE_END, DEPLOY_END
}
