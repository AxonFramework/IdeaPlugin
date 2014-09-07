package org.axonframework.intellij.ide.plugin.eventpublisher;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;

interface EventPublisherProvider {

    void scanPublishers(Project project, GlobalSearchScope scope, Registrar registrar);

    EventPublisher resolve(PsiElement element);

    interface Registrar {

        void registerPublisher(EventPublisher eventPublisher);

    }
}
