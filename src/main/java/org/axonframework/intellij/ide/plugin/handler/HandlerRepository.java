package org.axonframework.intellij.ide.plugin.handler;

import com.intellij.psi.PsiType;

import java.util.Set;

public interface HandlerRepository {

    void registerHandler(Handler eventHandler);

    Set<Handler> findHandlers(PsiType eventType);

    Set<Handler> findEventHandlers(PsiType eventType);

    Set<Handler> findCommandHandlers(PsiType eventType);
}
