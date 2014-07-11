package org.axonframework.intellij.ide.plugin;

import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiImmediateClassType;

public class CommandHandler {

    public static final String AXONFRAMEWORK_COMMANDHANDLING_ANNOTATION = "org.axonframework.commandhandling.annotation.CommandHandler";
    private final PsiType[] expressionTypes;

    public CommandHandler(PsiType[] expressionTypes) {
        this.expressionTypes = expressionTypes;
    }

    public boolean canHandleEvent(EventHandler eventHandler) {
        if (eventHandler == null) {
            return false;
        }

        PsiType[] eventHandlerArgumentArguments = eventHandler.getArguments();
        if (expressionTypes == null || eventHandlerArgumentArguments == null || eventHandlerArgumentArguments.length != expressionTypes.length) {
            return false;
        }
        for (int i = 0; i < eventHandlerArgumentArguments.length; i++) {
            if (eventHandlerArgumentArguments[i] == null || expressionTypes[i] == null ||
                    !(eventHandlerArgumentArguments[i].isAssignableFrom(expressionTypes[i]) ||
                            eventHandlerArgumentArguments[i] instanceof PsiImmediateClassType
                                    && ((PsiImmediateClassType) eventHandlerArgumentArguments[i]).getParameters()[0].isAssignableFrom(expressionTypes[i]))) {
                return false;
            }
        }
        return true;
    }
}
