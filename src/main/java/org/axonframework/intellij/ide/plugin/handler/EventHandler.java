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

    /**
     * Returns true if this EventHandler can handle an event of type eventType.
     *
     * @param eventType the type of the
     * @return true if eventType can be handled
     */
    boolean canHandle(PsiType eventType);

    /**
     * True if the eventHandler can still be accessed on disk.
     *
     * @return true if the eventHandler can still be accessed on disk
     */
    boolean isValid();

    /**
     * @return true if eventHandler method is declared on a subclass of AggregateRoot or AbstractEntity
     */
    boolean isInternalEvent();

    boolean isSagaEvent();
}
