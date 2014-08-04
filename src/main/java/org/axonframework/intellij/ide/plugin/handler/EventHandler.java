package org.axonframework.intellij.ide.plugin.handler;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;

public interface EventHandler {

    /**
     * Returns the PsiType describing the type of Event handled by this handler
     *
     * @return the PsiType describing the type of Event handled by this handler
     */
    PsiType getHandledType();

    /**
     * Returns the PsiElement of this handler to which the gutter icon may be assigned
     *
     * @return the PsiElement of this handler to which the gutter icon may be assigned
     */
    PsiElement getElementForAnnotation();

    boolean canHandle(PsiType eventType);

    boolean isValid();
}
