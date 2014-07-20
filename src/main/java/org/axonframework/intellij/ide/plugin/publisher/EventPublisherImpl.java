package org.axonframework.intellij.ide.plugin.publisher;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiImmediateClassType;

public class EventPublisherImpl implements EventPublisher {

    private final PsiType expressionType;
    private final PsiElement psiElement;

    public EventPublisherImpl(PsiType expressionType, PsiElement psiElement) {
        this.expressionType = expressionType;
        this.psiElement = psiElement;
    }

    @Override
    public boolean canPublishType(PsiType eventType) {
        return eventType != null
                && !(expressionType == null)
                && (eventType.isAssignableFrom(expressionType)
                || eventType instanceof PsiImmediateClassType
                && ((PsiImmediateClassType) eventType).getParameters()[0].isAssignableFrom(expressionType));

    }

    @Override
    public PsiElement getPsiElement() {
        return psiElement;
    }

    @Override
    public String toString() {
        return "EventPublisher{" +
                "psiElement=" + psiElement +
                '}';
    }
}
