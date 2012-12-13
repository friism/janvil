package com.heroku.janvil;

/**
* @author Ryan Brainard
*/
public final class Config {

    private final String apiKey;
    private Protocol protocol = Protocol.HTTPS;
    private String consumersUserAgent = null;
    private EventSubscription<Janvil.Event> eventSubscription;
    private boolean writeSlugUrl = false;
    private boolean writeCacheUrl = true;
    private boolean readCacheUrl = true;
    private String herokuUser;
    private String herokuApp;
    private boolean parallelUploads = false;

    public Config(String apiKey) {
        this.apiKey = apiKey;
        this.eventSubscription = new EventSubscription<Janvil.Event>(Janvil.Event.class);
        this.protocol = Config.Protocol.valueOf(AbstractApi.getPropOrEnvOrElse(
                "janvil.defaultProtocol",
                "JANVIL_DEFAULT_PROTOCOL",
                Config.Protocol.HTTPS.name()));
    }

    public Config setProtocol(Protocol protocol) {
        this.protocol = protocol;
        return this;
    }

    public Config setConsumersUserAgent(String consumersUserAgent) {
        this.consumersUserAgent = consumersUserAgent;
        return this;
    }

    public Config setEventSubscription(EventSubscription<Janvil.Event> eventSubscription) {
        this.eventSubscription = eventSubscription;
        return this;
    }

    public Config setWriteSlugUrl(boolean writeSlugUrl) {
        this.writeSlugUrl = writeSlugUrl;
        return this;
    }

    public Config setWriteCacheUrl(boolean writeCacheUrl) {
        this.writeCacheUrl = writeCacheUrl;
        return this;
    }

    public Config setReadCacheUrl(boolean readCacheUrl) {
        this.readCacheUrl = readCacheUrl;
        return this;
    }

    public Config setHerokuUser(String herokuUser) {
        this.herokuUser = herokuUser;
        return this;
    }

    public Config setHerokuApp(String herokuApp) {
        this.herokuApp = herokuApp;
        return this;
    }

    public Config setParallelUploads(boolean parallelUploads) {
        this.parallelUploads = parallelUploads;
        return this;
    }

    String getApiKey() {
        return apiKey;
    }

    Protocol getProtocol() {
        return protocol;
    }

    String getConsumersUserAgent() {
        return consumersUserAgent;
    }

    EventSubscription<Janvil.Event> getEventSubscription() {
        return eventSubscription;
    }

    boolean getWriteSlugUrl() {
        return writeSlugUrl;
    }

    boolean getWriteCacheUrl() {
        return writeCacheUrl;
    }

    boolean getReadCacheUrl() {
        return readCacheUrl;
    }

    String getHerokuUser() {
        return herokuUser;
    }

    String getHerokuApp() {
        return herokuApp;
    }

    public boolean isParallelUploads() {
        return parallelUploads;
    }

    public static enum Protocol {
        HTTP("http"),
        HTTPS("https");

        final String scheme;

        Protocol(String scheme) {
            this.scheme = scheme;
        }
    }
}
