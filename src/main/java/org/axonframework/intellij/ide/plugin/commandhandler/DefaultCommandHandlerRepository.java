package org.axonframework.intellij.ide.plugin.commandhandler;

import com.intellij.psi.PsiType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

class DefaultCommandHandlerRepository implements CommandHandlerRepository {

    private final List<CommandHandler> handlers = new CopyOnWriteArrayList<CommandHandler>();

    public void registerHandler(CommandHandler eventHandler) {
        handlers.add(eventHandler);
        List<CommandHandler> invalidated = new ArrayList<CommandHandler>();
        for (CommandHandler entry : handlers) {
            if (!entry.isValid()) {
                invalidated.add(entry);
            }
        }
        handlers.removeAll(invalidated);
    }

    @Override
    public Set<CommandHandler> findHandlers(PsiType eventType) {
        Set<CommandHandler> found = new HashSet<CommandHandler>();
        for (CommandHandler eventHandler : handlers) {
            if (eventHandler.isValid() && eventHandler.canHandle(eventType)) {
                found.add(eventHandler);
            }
        }
        return found;
    }
}
