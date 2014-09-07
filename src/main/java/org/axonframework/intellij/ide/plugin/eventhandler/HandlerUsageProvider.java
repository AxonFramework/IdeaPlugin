package org.axonframework.intellij.ide.plugin.eventhandler;

import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiParameter;
import org.axonframework.intellij.ide.plugin.eventpublisher.PublisherProviderManager;

class HandlerUsageProvider implements ImplicitUsageProvider {

    @Override
    public boolean isImplicitUsage(PsiElement element) {
        final Project project = element.getProject();

        return isEventParameter(element, project) || isHandlerMethod(element, project);
    }

    private boolean isHandlerMethod(PsiElement element, Project project) {
        EventHandler handler = HandlerProviderManager.getInstance(project)
                                                     .resolveEventHandler(element);
        return handler != null
                && !PublisherProviderManager.getInstance(project)
                                            .getRepository()
                                            .getPublishersFor(handler.getHandledType()).isEmpty();
    }

    private boolean isEventParameter(PsiElement element, Project project) {
        if (element instanceof PsiParameter) {
            PsiElement method = ((PsiParameter) element).getDeclarationScope();
            EventHandler handler = HandlerProviderManager.getInstance(project)
                                                         .resolveEventHandler(method);
            if (handler != null && ((PsiParameter) element).getType().equals(handler.getHandledType())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isImplicitRead(PsiElement element) {
        return false;
    }

    @Override
    public boolean isImplicitWrite(PsiElement element) {
        return false;
    }
}
