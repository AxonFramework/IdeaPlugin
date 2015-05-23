package org.axonframework.intellij.ide.plugin.publisher;

import com.intellij.psi.PsiType;

import java.util.Set;

public interface PublisherRepository {

    void registerPublisher(Publisher eventPublisher);

    Set<Publisher> getPublishersFor(PsiType type);
}
