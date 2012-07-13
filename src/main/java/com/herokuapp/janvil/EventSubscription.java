package com.herokuapp.janvil;

import java.util.*;

/**
 * @author Ryan Brainard
 */
public final class EventSubscription {

    public static enum Event {
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

    public static interface Subscriber {
        void handle(Event event, Object data);
    }

    private final Map<Event, Set<Subscriber>> subscribers = new EnumMap<Event, Set<Subscriber>>(Event.class);

    void announce(Event event) {
        announce(event, null);
    }

    void announce(Event event, Object data) {
        if (subscribers.containsKey(event)) {
            for (Subscriber subscriber : subscribers.get(event)) {
                subscriber.handle(event, data);
            }
        }
    }

    public EventSubscription subscribe(Event event, Subscriber subscriber) {
        return subscribe(EnumSet.of(event), subscriber);
    }

    public EventSubscription subscribe(EnumSet<Event> events, Subscriber subscriber) {
        for (Event event : events) {
            if (!subscribers.containsKey(event)) {
                subscribers.put(event, new HashSet<Subscriber>());
            }
            subscribers.get(event).add(subscriber);
        }

        return this;
    }
}
