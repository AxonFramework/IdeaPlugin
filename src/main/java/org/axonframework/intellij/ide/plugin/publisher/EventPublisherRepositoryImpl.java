package org.axonframework.intellij.ide.plugin.publisher;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class EventPublisherRepositoryImpl implements EventPublisherRepository {

    private final ConcurrentMap<PsiElement, EventPublisher> publishers = new ConcurrentHashMap<PsiElement, EventPublisher>();

    public void registerPublisher(EventPublisher eventPublisher) {
        publishers.put(eventPublisher.getPsiElement(), eventPublisher);
        for (Map.Entry<PsiElement, EventPublisher> entry : publishers.entrySet()) {
            if (!entry.getValue().isValid()) {
                publishers.remove(entry.getKey());
            }
        }
    }

    @Override
    public Set<EventPublisher> getPublishersFor(PsiType type) {
        Set<EventPublisher> foundPublishers = new HashSet<EventPublisher>();
        for (EventPublisher publisher : new ArrayList<EventPublisher>(publishers.values())) {
            if (publisher.isValid() && publisher.canPublishType(type)) {
                foundPublishers.add(publisher);
            }
        }
        return foundPublishers;
    }

    public EventPublisher getPublisher(PsiElement psiElement) {
        return publishers.get(psiElement);
    }
}
