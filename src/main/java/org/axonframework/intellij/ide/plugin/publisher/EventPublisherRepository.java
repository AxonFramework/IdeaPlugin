package org.axonframework.intellij.ide.plugin.publisher;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;

import java.util.Collection;
import java.util.List;

public interface EventPublisherRepository {

    void addPublisherForType(PsiType type, EventPublisher eventPublisher);

    List<PsiElement> getPublisherPsiElementsFor(PsiType type);

    Collection<EventPublisher> getAllPublishers();
}
