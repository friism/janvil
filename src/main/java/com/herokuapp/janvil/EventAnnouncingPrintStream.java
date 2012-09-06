package com.herokuapp.janvil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
* @author Ryan Brainard
*/
class EventAnnouncingPrintStream extends PrintStream {
    public EventAnnouncingPrintStream(final EventSubscription<Janvil.Event> eventSubscription) {
        super(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                eventSubscription.announce(Janvil.Event.HTTP_LOGGING_BYTE, b);
            }
        });
    }
}
