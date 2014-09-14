package org.axonframework.intellij.ide.plugin.handler;

import com.intellij.psi.PsiType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

class DefaultEventHandlerRepository implements EventHandlerRepository {

    private final List<EventHandler> handlers = new CopyOnWriteArrayList<EventHandler>();

    public void registerHandler(EventHandler eventHandler) {
        handlers.add(eventHandler);
        List<EventHandler> invalidated = new ArrayList<EventHandler>();
        for (EventHandler entry : handlers) {
            if (!entry.isValid()) {
                invalidated.add(entry);
            }
        }
        handlers.removeAll(invalidated);
    }

    @Override
    public Set<EventHandler> findHandlers(PsiType eventType) {
        Set<EventHandler> found = new HashSet<EventHandler>();
        for (EventHandler eventHandler : handlers) {
            if (eventHandler.isValid() && eventHandler.canHandle(eventType)) {
                found.add(eventHandler);
            }
        }
        return found;
    }
}
