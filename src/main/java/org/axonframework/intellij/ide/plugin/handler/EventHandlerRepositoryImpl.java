package org.axonframework.intellij.ide.plugin.handler;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class EventHandlerRepositoryImpl implements EventHandlerRepository {

    private Multimap<PsiType, EventHandler> handlersThatCanHandleType = HashMultimap.create();

    @Override
    public void addHandlerForType(PsiType type, EventHandler eventHandler) {
        handlersThatCanHandleType.put(type, eventHandler);
    }

    @Override
    public Collection<EventHandler> getAllHandlers() {
        return handlersThatCanHandleType.values();
    }

    @Override
    public Set<PsiMethod> getAllHandlerPsiElements() {
        Collection<EventHandler> handlers = handlersThatCanHandleType.values();
        Set<PsiMethod> psiElements = new HashSet<PsiMethod>();
        for (EventHandler handler : handlers) {
            psiElements.add(handler.getPsiMethod());
        }

        return psiElements;
    }

    @Override
    public String toString() {
        return "EventHandlerRepositoryImpl{" +
                "handlersThatCanHandleType=" + handlersThatCanHandleType +
                '}';
    }
}
