package org.axonframework.intellij.ide.plugin.publisher;

import com.intellij.psi.PsiType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

class DefaultEventPublisherRepository implements PublisherRepository {

    private final List<Publisher> publishers = new CopyOnWriteArrayList<Publisher>();

    public void registerPublisher(Publisher eventPublisher) {
        publishers.add(eventPublisher);
        List<Publisher> invalidated = new ArrayList<Publisher>();
        for (Publisher entry : publishers) {
            if (!entry.isValid()) {
                invalidated.add(entry);
            }
        }
        publishers.remove(invalidated);
    }

    @Override
    public Set<Publisher> getPublishersFor(PsiType type) {
        Set<Publisher> foundPublishers = new HashSet<Publisher>();
        for (Publisher publisher : publishers) {
            if (publisher.isValid() && publisher.canPublishType(type)) {
                foundPublishers.add(publisher);
            }
        }
        return foundPublishers;
    }

}
