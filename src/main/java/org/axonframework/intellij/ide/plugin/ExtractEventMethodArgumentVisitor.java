package org.axonframework.intellij.ide.plugin;

import com.intellij.psi.*;
import org.axonframework.intellij.ide.plugin.handler.EventHandler;
import org.axonframework.intellij.ide.plugin.handler.EventHandlerImpl;

/**
 */
public class ExtractEventMethodArgumentVisitor extends JavaRecursiveElementVisitor {

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
