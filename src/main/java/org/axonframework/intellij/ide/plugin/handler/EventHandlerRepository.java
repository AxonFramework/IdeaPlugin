package org.axonframework.intellij.ide.plugin.handler;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;

import java.util.Collection;
import java.util.Set;

public interface EventHandlerRepository {

    void addHandlerForType(PsiType type, EventHandler eventHandler);

    Collection<EventHandler> getAllHandlers();

    Set<PsiElement> getAllHandlerPsiElements();
}
