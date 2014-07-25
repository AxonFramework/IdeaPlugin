package org.axonframework.intellij.ide.plugin.publisher;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiImmediateClassType;

public class EventPublisherImpl implements EventPublisher {

    private final PsiType publishedType;
    private final PsiElement psiElement;

    public EventPublisherImpl(PsiType publishedType, PsiElement psiElement) {
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
    public PsiMethod getEnclosingMethod() {
        PsiElement parent = psiElement.getParent();
        while (parent != null && !(parent instanceof PsiMethod)) {
            parent = parent.getParent();
        }
        return (PsiMethod) parent;
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

        EventPublisherImpl that = (EventPublisherImpl) o;

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
