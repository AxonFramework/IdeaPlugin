package org.axonframework.intellij.ide.plugin.commandhandler;

import com.intellij.psi.PsiType;

import java.util.Set;

public interface CommandHandlerRepository {

    void registerHandler(CommandHandler eventHandler);

    Set<CommandHandler> findHandlers(PsiType eventType);
}
