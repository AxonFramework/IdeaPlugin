package org.axonframework.intellij.ide.plugin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;

public class HandledEventsRepository {

    private Multimap<EventPublisher, EventHandler> publisherEventHandlerMap = HashMultimap.create();
    private Multimap<EventHandler, EventPublisher> eventToPublisherMap = HashMultimap.create();

    public void addHandledEvent(EventPublisher eventPublisher, EventHandler eventHandler) {
        publisherEventHandlerMap.put(eventPublisher, eventHandler);
        eventToPublisherMap.put(eventHandler, eventPublisher);
    }

    public Collection<EventHandler> getEventHandlersFor(EventPublisher publisher) {
        return publisherEventHandlerMap.get(publisher);
    }

    public Collection<EventPublisher> getEventPublishersFor(EventHandler handler) {
        return eventToPublisherMap.get(handler);
    }
}
