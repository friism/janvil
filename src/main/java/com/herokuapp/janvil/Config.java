package com.herokuapp.janvil;

/**
* @author Ryan Brainard
*/
public final class Config {

    private final String apiKey;
    private Protocol protocol = Protocol.HTTPS;
    private String consumersUserAgent = null;
    private EventSubscription<Janvil.Event> eventSubscription;

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

    public String getApiKey() {
        return apiKey;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public String getConsumersUserAgent() {
        return consumersUserAgent;
    }

    public EventSubscription<Janvil.Event> getEventSubscription() {
        return eventSubscription;
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
