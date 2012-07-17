package com.herokuapp.janvil;

import java.util.*;

/**
 * @author Ryan Brainard
 */
public final class EventSubscription<E extends Enum<E>> {

    public static interface Subscriber {
        void handle(DeployEvent event, Object data);
    }

    private final Map<DeployEvent, Set<Subscriber>> subscribers = new EnumMap<DeployEvent, Set<Subscriber>>(DeployEvent.class);

    void announce(DeployEvent event) {
        announce(event, null);
    }

    void announce(DeployEvent event, Object data) {
        if (subscribers.containsKey(event)) {
            for (Subscriber subscriber : subscribers.get(event)) {
                subscriber.handle(event, data);
            }
        }
    }

    public EventSubscription subscribe(DeployEvent event, Subscriber subscriber) {
        return subscribe(EnumSet.of(event), subscriber);
    }

    public EventSubscription subscribe(EnumSet<DeployEvent> events, Subscriber subscriber) {
        for (DeployEvent event : events) {
            if (!subscribers.containsKey(event)) {
                subscribers.put(event, new HashSet<Subscriber>());
            }
            subscribers.get(event).add(subscriber);
        }

        return this;
    }
}
