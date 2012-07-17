package com.herokuapp.janvil;

/**
* @author Ryan Brainard
*/
public final class Config {

    private final String apiKey;
    private Protocol protocol = Protocol.HTTPS;
    private String consumersUserAgent = null;

    public Config(String apiKey) {
        this.apiKey = apiKey;
    }

    public Config setProtocol(Protocol protocol) {
        this.protocol = protocol;
        return this;
    }

    public Config setConsumersUserAgent(String consumersUserAgent) {
        this.consumersUserAgent = consumersUserAgent;
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

    public static enum Protocol {
        HTTP("http"),
        HTTPS("https");

        final String scheme;

        Protocol(String scheme) {
            this.scheme = scheme;
        }
    }
}
