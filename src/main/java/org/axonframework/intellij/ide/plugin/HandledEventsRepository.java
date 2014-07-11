package org.axonframework.intellij.ide.plugin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.intellij.psi.PsiElement;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class HandledEventsRepository {

    private Multimap<EventPublisher, EventHandler> publisherEventHandlerMap = HashMultimap.create();
    private Multimap<EventHandler, EventPublisher> eventToPublisherMap = HashMultimap.create();

    public void addHandledEvent(EventPublisher eventPublisher, EventHandler eventHandler) {
        publisherEventHandlerMap.put(eventPublisher, eventHandler);
        eventToPublisherMap.put(eventHandler, eventPublisher);
    }

    public Set<PsiElement> getEventHandlersFor(EventPublisher publisher) {
        Collection<EventHandler> eventHandlers = publisherEventHandlerMap.get(publisher);

        Set<PsiElement> targets = new HashSet<PsiElement>();
        for (EventHandler eventHandler : eventHandlers) {
            targets.add(eventHandler.getPsiElement().getParent().getParent());
        }
        return targets;
    }

    public Set<PsiElement> getEventPublishersFor(EventHandler handler) {
        Collection<EventPublisher> eventPublishers = eventToPublisherMap.get(handler);

        Set<PsiElement> targets = new HashSet<PsiElement>();
        for (EventPublisher eventHandler : eventPublishers) {
            targets.add(eventHandler.getPsiElement());
        }
        return targets;
    }

    public Collection<EventHandler> getAllEventHandlers() {
        return eventToPublisherMap.keySet();
    }

    @Override
    public String toString() {
        return "HandledEventsRepository{" +
                "publisherEventHandlerMap=" + publisherEventHandlerMap +
                ", eventToPublisherMap=" + eventToPublisherMap +
                '}';
    }
}
