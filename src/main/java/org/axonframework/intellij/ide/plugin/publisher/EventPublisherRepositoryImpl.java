package org.axonframework.intellij.ide.plugin.publisher;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import org.axonframework.intellij.ide.plugin.handler.EventHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EventPublisherRepositoryImpl {

    private Multimap<EventPublisher, EventHandler> publisherEventHandlerMap = HashMultimap.create();
    private Multimap<EventHandler, EventPublisher> eventToPublisherMap = HashMultimap.create();

    private Multimap<PsiType, EventPublisher> publishersThatCanHandleType = HashMultimap.create();

    public void addHandledEvent(EventPublisher eventPublisher, EventHandler eventHandler) {
        publisherEventHandlerMap.put(eventPublisher, eventHandler);
        eventToPublisherMap.put(eventHandler, eventPublisher);
    }

    public void addPublisherForType(PsiType type, EventPublisher eventPublisher) {
        publishersThatCanHandleType.put(type, eventPublisher);
    }

    public Set<PsiElement> getPublisherPsiElementsFor(PsiType type) {
        if (!publishersThatCanHandleType.containsKey(type)) {
             return Collections.emptySet();
        }

        Collection<EventPublisher> publishers = publishersThatCanHandleType.get(type);
        Set<PsiElement> psiElements = new HashSet<PsiElement>();
        for (EventPublisher publisher : publishers) {
            psiElements.add(publisher.getPsiElement());
        }

        return psiElements;
    }

    public Set<PsiElement> getEventHandlersFor(EventPublisher publisher) {
        Collection<EventHandler> eventHandlers = publisherEventHandlerMap.get(publisher);

        Set<PsiElement> targets = new HashSet<PsiElement>();
        for (EventHandler eventHandler : eventHandlers) {
            targets.add(eventHandler.getPsiElement().getParent().getParent());
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
