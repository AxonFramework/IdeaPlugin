package org.axonframework.intellij.ide.plugin.publisher;

import com.intellij.psi.PsiType;

import java.util.Set;

public interface EventPublisherRepository {

    void registerPublisher(EventPublisher eventPublisher);

    Set<EventPublisher> getPublishersFor(PsiType type);
}
