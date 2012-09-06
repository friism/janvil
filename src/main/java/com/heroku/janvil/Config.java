package com.heroku.janvil;

/**
* @author Ryan Brainard
*/
public final class Config {

    private final String apiKey;
    private Protocol protocol = Protocol.HTTPS;
    private String consumersUserAgent = null;
    private EventSubscription<Janvil.Event> eventSubscription;
    private boolean writeMetadata = true;
    private boolean readMetadata = true;

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

    public Config setWriteMetadata(boolean writeMetadata) {
        this.writeMetadata = writeMetadata;
        return this;
    }

    public Config setReadMetadata(boolean readMetadata) {
        this.readMetadata = readMetadata;
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

    boolean getWriteMetadata() {
        return writeMetadata;
    }

    boolean getReadMetadata() {
        return readMetadata;
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
