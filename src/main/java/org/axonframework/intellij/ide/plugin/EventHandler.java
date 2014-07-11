package org.axonframework.intellij.ide.plugin;

import com.intellij.psi.*;

public class EventHandler {

    public static final String AXONFRAMEWORK_EVENTHANDLING_ANNOTATION = "org.axonframework.eventhandling.annotation.EventHandler";
    public static final String EVENT_HANDLER_ARGUMENT = "eventType";
    private final PsiType[] annotationArguments;

    public EventHandler(PsiAnnotationMemberValue eventType) {
        this.annotationArguments = getAnnotationArguments(eventType);
    }

    public EventHandler(PsiMethod method) {
        this.annotationArguments = getMethodArguments(method);
    }

    public PsiType[] getArguments() {
        return annotationArguments;
    }

    private PsiType[] getMethodArguments(PsiMethod method) {
        PsiParameterList list = method.getParameterList();
        PsiType[] argument = new PsiType[list.getParametersCount()];
        for (int i = 0; i < list.getParameters().length; i++) {
            PsiParameter psiParameter = list.getParameters()[i];
            argument[i] = psiParameter.getType();
        }
        return argument;
    }

    private PsiType[] getAnnotationArguments(PsiAnnotationMemberValue eventType) {
        if (eventType.getChildren().length > 0 && eventType.getFirstChild().getChildren().length > 0) {
            if (eventType instanceof PsiExpression) {
                PsiType typeOfArgument = ((PsiExpression) eventType).getType();
                if (typeIsKnown(typeOfArgument)) {
                    return new PsiType[]{typeOfArgument};
                }
            }
        }
        return new PsiType[]{};
    }

    private boolean typeIsKnown(PsiType type) {
        return type != null;
    }

    public static boolean isEventHandlerAnnotation(PsiAnnotation psiAnnotation) {
        return psiAnnotation.getText().contains("@EventHandler");
    }
}
