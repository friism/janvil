package com.herokuapp.janvil;

import java.util.*;

/**
 * @author Ryan Brainard
 */
public final class EventSubscription<E extends Enum<E>> {

    public static interface Subscriber<E> {
        void handle(E event, Object data);
    }

    private final Map<E, Set<Subscriber<E>>> subscribers;

    public EventSubscription(Class<E> eClass) {
        subscribers = new EnumMap<E, Set<Subscriber<E>>>(eClass);
    }

    void announce(E event) {
        announce(event, null);
    }

    void announce(E event, Object data) {
        if (subscribers.containsKey(event)) {
            for (Subscriber<E> subscriber : subscribers.get(event)) {
                subscriber.handle(event, data);
            }
        }
    }

    public EventSubscription<E> subscribe(E event, Subscriber<E> subscriber) {
        return subscribe(EnumSet.of(event), subscriber);
    }

    public EventSubscription<E> subscribe(EnumSet<E> events, Subscriber<E> subscriber) {
        for (E event : events) {
            if (!subscribers.containsKey(event)) {
                subscribers.put(event, new HashSet<Subscriber<E>>());
            }
            subscribers.get(event).add(subscriber);
        }

        return this;
    }

    public Set<E> getSubscribedEvents() {
        return subscribers.keySet();
    }
}
