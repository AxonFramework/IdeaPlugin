package org.axonframework.intellij.ide.plugin;

import com.intellij.psi.*;

/**
 */
public class ExtractEventMethodArgumentVisitor extends JavaRecursiveElementVisitor {

    private EventHandler eventHandler;

    @Override
    public void visitMethod(PsiMethod method) {
        eventHandler = EventHandler.createEventHandler(method);
        super.visitMethod(method);
    }

    public EventHandler getEventHandler() {
        return eventHandler;
    }
}
