package org.axonframework.intellij.ide.plugin.publisher;

import com.intellij.psi.*;

public class CommandEventPublisher implements EventPublisher {

    private final PsiElement psiElement;
    private PsiClass commandType;

    public CommandEventPublisher(PsiClass psiClass, PsiElement psiElement) {
        this.commandType = psiClass;
        this.psiElement = psiElement;
    }

    @Override
    public boolean canPublishType(PsiType eventType) {
        return eventType.getCanonicalText().equals(commandType.getQualifiedName());
    }

    @Override
    public PsiType getPublishedType() {
        return null;
    }

    @Override
    public PsiElement getPsiElement() {
        return psiElement;
    }

    @Override
    public boolean isValid() {
        return psiElement.isValid();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommandEventPublisher that = (CommandEventPublisher) o;

        if (psiElement != null ? !psiElement.equals(that.psiElement) : that.psiElement != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return psiElement != null ? psiElement.hashCode() : 0;
    }
}
