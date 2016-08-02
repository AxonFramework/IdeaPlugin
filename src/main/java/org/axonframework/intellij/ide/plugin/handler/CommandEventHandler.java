package org.axonframework.intellij.ide.plugin.handler;

import com.intellij.psi.*;

public class CommandEventHandler implements Handler {

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

    public static Handler createEventHandler(PsiMethod method) {
        PsiType[] methodArguments = getMethodArguments(method);
        return methodArguments.length > 0 ? new CommandEventHandler(methodArguments[0], method) : null;
    }

    private static PsiType[] getMethodArguments(PsiMethod method) {
        PsiParameter[] listParameters = method.getParameterList().getParameters();
        PsiType[] argument = new PsiType[listParameters.length];
        for (int i = 0; i < listParameters.length; i++) {
            argument[i] = listParameters[i].getType();
        }
        return argument;
    }
}
