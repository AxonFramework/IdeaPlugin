package org.axonframework.intellij.ide.plugin.handler;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;

/**
 * @author Allard Buijze
 */
public interface EventHandlerProvider {

    void scanHandlers(Project project, GlobalSearchScope scope, Registrar registrar);

    EventHandler resolve(PsiElement element);

    interface Registrar {

        void registerHandler(EventHandler eventHandler);

    }
}
