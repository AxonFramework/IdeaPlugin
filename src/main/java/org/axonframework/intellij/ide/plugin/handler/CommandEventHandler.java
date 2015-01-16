package org.axonframework.intellij.ide.plugin.handler;

import com.intellij.psi.*;

public class CommandEventHandler implements EventHandler {

    private final PsiType commandType;
    private final PsiMethod method;

    private CommandEventHandler(PsiType psiClassType, PsiMethod method) {
        this.commandType = psiClassType;
        this.method = method;
    }

    @Override
    public PsiType getHandledType() {
        return commandType;
    }

    @Override
    public PsiElement getElementForAnnotation() {
        return method.getNameIdentifier();
    }

    @Override
    public boolean canHandle(PsiType eventType) {
        return eventType != null && eventType.isAssignableFrom(commandType);
    }

    @Override
    public boolean isValid() {
        return commandType.isValid();
    }

    @Override
    public boolean isInternalEvent() {
        return false;
    }

    @Override
    public boolean isSagaEvent() {
        return false;
    }

    public static EventHandler createEventHandler(PsiMethod method) {
        PsiType[] methodArguments = getMethodArguments(method);
        return new CommandEventHandler(methodArguments[0], method);
    }

    private static PsiType[] getMethodArguments(PsiMethod method) {
        PsiParameterList list = method.getParameterList();
        PsiType[] argument = new PsiType[list.getParametersCount()];
        for (int i = 0; i < list.getParameters().length; i++) {
            PsiParameter psiParameter = list.getParameters()[i];
            argument[i] = psiParameter.getType();
        }
        return argument;
    }
}
