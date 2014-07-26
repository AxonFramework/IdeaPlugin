package org.axonframework.intellij.ide.plugin.publisher;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiImmediateClassType;

class DefaultEventPublisher implements EventPublisher {

    private final PsiType publishedType;
    private final PsiElement psiElement;

    public DefaultEventPublisher(PsiType publishedType, PsiElement psiElement) {
        this.publishedType = publishedType;
        this.psiElement = psiElement;
    }

    @Override
    public boolean canPublishType(PsiType eventType) {
        return eventType != null
                && !(publishedType == null)
                && (eventType.isAssignableFrom(publishedType)
                || eventType instanceof PsiImmediateClassType
                && ((PsiImmediateClassType) eventType).getParameters()[0].isAssignableFrom(publishedType));

    }

    @Override
    public PsiType getPublishedType() {
        return publishedType;
    }

    @Override
    public PsiElement getPsiElement() {
        return psiElement;
    }

    @Override
    public boolean isValid() {
        return psiElement.isValid() && publishedType.isValid();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultEventPublisher that = (DefaultEventPublisher) o;

        return psiElement.equals(that.psiElement);
    }

    @Override
    public int hashCode() {
        return psiElement.hashCode();
    }

    @Override
    public String toString() {
        return "EventPublisher{" +
                "psiElement=" + psiElement +
                '}';
    }
}
