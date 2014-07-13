package org.axonframework.intellij.ide.plugin.publisher;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;

import java.util.Set;

/**
 * Created by rinokadijk on 13/07/14.
 */
public interface EventPublisherRepository {
    void addPublisherForType(PsiType type, EventPublisher eventPublisher);

    Set<PsiElement> getPublisherPsiElementsFor(PsiType type);
}
