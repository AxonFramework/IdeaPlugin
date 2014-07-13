package org.axonframework.intellij.ide.plugin.publisher;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EventPublisherRepositoryImpl implements EventPublisherRepository {

    private Multimap<PsiType, EventPublisher> publishersThatCanPublishType = HashMultimap.create();

    @Override
    public void addPublisherForType(PsiType type, EventPublisher eventPublisher) {
        publishersThatCanPublishType.put(type, eventPublisher);
    }

    @Override
    public Set<PsiElement> getPublisherPsiElementsFor(PsiType type) {
        if (!publishersThatCanPublishType.containsKey(type)) {
             return Collections.emptySet();
        }

        Collection<EventPublisher> publishers = publishersThatCanPublishType.get(type);
        Set<PsiElement> psiElements = new HashSet<PsiElement>();
        for (EventPublisher publisher : publishers) {
            psiElements.add(publisher.getPsiElement());
        }

        return psiElements;
    }

    @Override
    public String toString() {
        return "EventPublisherRepositoryImpl{" +
                "publishersThatCanPublishType=" + publishersThatCanPublishType +
                '}';
    }
}
