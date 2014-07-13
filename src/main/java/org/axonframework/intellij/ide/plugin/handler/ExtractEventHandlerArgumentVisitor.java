package org.axonframework.intellij.ide.plugin.handler;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiMethod;

/**
 */
public class ExtractEventHandlerArgumentVisitor extends JavaRecursiveElementVisitor {

    private EventHandlerImpl eventHandler;

    @Override
    public void visitMethod(PsiMethod method) {
        eventHandler = EventHandlerImpl.createEventHandler(method);
        super.visitMethod(method);
    }

    public EventHandler getEventHandler() {
        return eventHandler;
    }
}
