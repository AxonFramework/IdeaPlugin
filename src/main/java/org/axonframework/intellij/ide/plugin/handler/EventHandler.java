package org.axonframework.intellij.ide.plugin.handler;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
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

    /**
     * Returns the reference to the Method, to which icons in other locations should refer
     *
     * @return the reference to the Method, to which icons in other locations should refer
     */
    PsiMethod getPsiMethod();

    boolean canHandle(PsiType eventType);

    boolean isValid();
}
