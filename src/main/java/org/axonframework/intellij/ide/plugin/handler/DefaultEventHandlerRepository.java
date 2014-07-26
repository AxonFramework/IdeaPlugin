package org.axonframework.intellij.ide.plugin.handler;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class DefaultEventHandlerRepository implements EventHandlerRepository {

    private ConcurrentMap<PsiMethod, EventHandler> handlers = new ConcurrentHashMap<PsiMethod, EventHandler>();

    public void registerHandler(EventHandler eventHandler) {
        handlers.put(eventHandler.getPsiMethod(), eventHandler);
        for (Map.Entry<PsiMethod, EventHandler> entry : handlers.entrySet()) {
            if (!entry.getValue().isValid()) {
                handlers.remove(entry.getKey());
            }
        }
    }

    @Override
    public Set<EventHandler> findHandlers(PsiType eventType) {
        Set<EventHandler> found = new HashSet<EventHandler>();
        for (EventHandler eventHandler : new ArrayList<EventHandler>(handlers.values())) {
            if (eventHandler.isValid() && eventHandler.canHandle(eventType)) {
                found.add(eventHandler);
            }
        }
        return found;
    }
}
