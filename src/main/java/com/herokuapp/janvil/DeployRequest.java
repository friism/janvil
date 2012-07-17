package com.herokuapp.janvil;

import java.util.HashMap;

/**
* @author Ryan Brainard
*/
public final class DeployRequest {

    private Manifest manifest;
    private String appName;
    private HashMap<String, String> env;
    private String buildpack = "";
    private EventSubscription eventSubscription;

    public DeployRequest(Manifest manifest, String appName) {
        this.manifest = manifest;
        this.appName = appName;
        this.env = new HashMap<String, String>();
        this.eventSubscription = new EventSubscription();
    }

    public DeployRequest env(HashMap<String, String> env) {
        this.env = env;
        return this;
    }

    public DeployRequest buildpack(String buildpack) {
        this.buildpack = buildpack;
        return this;
    }

    public DeployRequest eventSubscription(EventSubscription eventSubscription) {
        this.eventSubscription = eventSubscription;
        return this;
    }

    public Manifest manifest() {
        return manifest;
    }

    public String appName() {
        return appName;
    }

    public HashMap<String, String> env() {
        return env;
    }

    public String buildpack() {
        return buildpack;
    }

    public EventSubscription eventSubscription() {
        return eventSubscription;
    }
}
