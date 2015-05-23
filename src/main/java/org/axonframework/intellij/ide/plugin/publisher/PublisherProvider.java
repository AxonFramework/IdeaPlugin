package org.axonframework.intellij.ide.plugin.publisher;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;

interface PublisherProvider {

    void scanPublishers(Project project, GlobalSearchScope scope, Registrar registrar);

    Publisher resolve(PsiElement element);

    interface Registrar {

        void registerPublisher(Publisher eventPublisher);

    }
}
