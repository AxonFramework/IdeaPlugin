package org.axonframework.intellij.ide.plugin.handler;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;

interface HandlerProvider {

    void scanHandlers(Project project, GlobalSearchScope scope, Registrar registrar);

    Handler resolve(PsiElement element, final Project project);

    interface Registrar {

        void registerHandler(Handler eventHandler);

    }
}
