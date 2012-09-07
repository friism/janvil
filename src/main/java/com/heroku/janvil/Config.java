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

    public Config(String apiKey) {
        this.apiKey = apiKey;
        this.eventSubscription = new EventSubscription<Janvil.Event>(Janvil.Event.class);
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

    public static enum Protocol {
        HTTP("http"),
        HTTPS("https");

        final String scheme;

        Protocol(String scheme) {
            this.scheme = scheme;
        }
    }
}
