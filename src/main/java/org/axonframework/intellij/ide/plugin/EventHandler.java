package org.axonframework.intellij.ide.plugin;

import com.intellij.psi.*;

public class EventHandler {

    public static final String AXONFRAMEWORK_EVENTHANDLING_ANNOTATION = "org.axonframework.eventhandling.annotation.EventHandler";
    public static final String EVENT_HANDLER_ARGUMENT = "eventType";

    private final PsiType[] annotationOrMethodArguments;
    private final PsiAnnotation annotation;


    private EventHandler(PsiAnnotation annotation, PsiMethod method) {
        this.annotation = annotation;
        this.annotationOrMethodArguments = getMethodArguments(method);
    }

    private EventHandler(PsiAnnotation annotation, PsiAnnotationMemberValue eventType) {
        this.annotation = annotation;
        this.annotationOrMethodArguments = getAnnotationArguments(eventType);
    }

    public PsiType[] getArguments() {
        return annotationOrMethodArguments;
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
        return psiAnnotation != null && AXONFRAMEWORK_EVENTHANDLING_ANNOTATION.equals(psiAnnotation.getQualifiedName());
    }

    public static EventHandler createEventHandler(PsiMethod method) {
        PsiAnnotation annotation = method.getModifierList().findAnnotation(EventHandler.AXONFRAMEWORK_EVENTHANDLING_ANNOTATION);

        if (!isEventHandlerAnnotation(annotation)) {
            return null;
        }

        PsiAnnotationMemberValue eventType = annotation.findAttributeValue(EventHandler.EVENT_HANDLER_ARGUMENT);
        if (annotationHasEventTypeArgument(eventType) && hasChildren(eventType)) {
            return new EventHandler(annotation, eventType);
        }
        return new EventHandler(annotation, method);
    }

    private static boolean annotationHasEventTypeArgument(PsiAnnotationMemberValue eventType) {
        return eventType instanceof PsiExpression &&
                !((PsiExpression) eventType).getType().getCanonicalText().equals("java.lang.Class<java.lang.Void>");
    }

    private static boolean hasChildren(PsiAnnotationMemberValue eventType) {
        return eventType.getChildren().length > 0 && eventType.getFirstChild().getChildren().length > 0;
    }

}
