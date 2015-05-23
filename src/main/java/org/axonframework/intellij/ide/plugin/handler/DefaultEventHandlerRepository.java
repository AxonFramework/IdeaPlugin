package org.axonframework.intellij.ide.plugin.handler;

import com.intellij.psi.PsiType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

class DefaultEventHandlerRepository implements HandlerRepository {

    private final List<Handler> handlers = new CopyOnWriteArrayList<Handler>();

    public void registerHandler(Handler eventHandler) {
        handlers.add(eventHandler);
        List<Handler> invalidated = new ArrayList<Handler>();
        for (Handler entry : handlers) {
            if (!entry.isValid()) {
                invalidated.add(entry);
            }
        }
        handlers.removeAll(invalidated);
    }

    @Override
    public Set<Handler> findHandlers(PsiType eventType) {
        Set<Handler> found = new HashSet<Handler>();
        for (Handler eventHandler : handlers) {
            if (eventHandler.isValid() && eventHandler.canHandle(eventType)) {
                found.add(eventHandler);
            }
        }
        return found;
    }

    @Override
    public Set<Handler> findEventHandlers(PsiType eventType) {
        Set<Handler> found = new HashSet<Handler>();
        for (Handler eventHandler : findHandlers(eventType)) {
            if (!(eventHandler instanceof CommandEventHandler)) {
                found.add(eventHandler);
            }
        }
        return found;
    }

    @Override
    public Set<Handler> findCommandHandlers(PsiType eventType) {
        Set<Handler> found = new HashSet<Handler>();
        for (Handler eventHandler : findHandlers(eventType)) {
            if (eventHandler instanceof CommandEventHandler) {
                found.add(eventHandler);
            }
        }
        return found;
    }
}
