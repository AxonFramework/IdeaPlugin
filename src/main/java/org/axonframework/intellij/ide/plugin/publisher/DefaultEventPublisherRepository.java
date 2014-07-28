package org.axonframework.intellij.ide.plugin.publisher;

import com.intellij.psi.PsiType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

class DefaultEventPublisherRepository implements EventPublisherRepository {

    private final List<EventPublisher> publishers = new CopyOnWriteArrayList<EventPublisher>();

    public void registerPublisher(EventPublisher eventPublisher) {
        publishers.add(eventPublisher);
        List<EventPublisher> invalidated = new ArrayList<EventPublisher>();
        for (EventPublisher entry : publishers) {
            if (!entry.isValid()) {
                invalidated.add(entry);
            }
        }
        publishers.remove(invalidated);
    }

    @Override
    public Set<EventPublisher> getPublishersFor(PsiType type) {
        Set<EventPublisher> foundPublishers = new HashSet<EventPublisher>();
        for (EventPublisher publisher : publishers) {
            if (publisher.isValid() && publisher.canPublishType(type)) {
                foundPublishers.add(publisher);
            }
        }
        return foundPublishers;
    }

}
