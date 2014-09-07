package org.axonframework.intellij.ide.plugin.commandhandler;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;

interface CommandHandlerProvider {

    void scanHandlers(Project project, GlobalSearchScope scope, Registrar registrar);

    CommandHandler resolve(PsiElement element);

    interface Registrar {

        void registerHandler(CommandHandler eventHandler);

    }
}
