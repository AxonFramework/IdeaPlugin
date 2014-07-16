package org.axonframework.intellij.ide.plugin.handler;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiType;

public class EventHandlerImpl implements EventHandler {

    public static final String AXONFRAMEWORK_EVENTHANDLING_ANNOTATION = "org.axonframework.eventhandling.annotation.EventHandler";
    public static final String EVENT_HANDLER_ARGUMENT = "eventType";

    private final PsiType[] annotationOrMethodArguments;
    private final PsiMethod method;


    private EventHandlerImpl(PsiAnnotation annotation, PsiMethod method) {
        this.method = method;
        this.annotationOrMethodArguments = getMethodArguments(method);
    }

    private EventHandlerImpl(PsiAnnotation annotation, PsiAnnotationMemberValue eventType, PsiMethod method) {
        this.method = method;
        this.annotationOrMethodArguments = getAnnotationArguments(eventType);
    }

    @Override
    public PsiType getHandledType() {
        if (annotationOrMethodArguments == null || annotationOrMethodArguments.length == 0) {
            return null;
        }
        return annotationOrMethodArguments[0];
    }

    @Override
    public PsiElement getElementForAnnotation() {
        return method.getNameIdentifier();
    }

    @Override
    public PsiMethod getPsiMethod() {
        return method;
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

    public static EventHandlerImpl createEventHandler(PsiMethod method) {
        PsiAnnotation annotation = method.getModifierList().findAnnotation(EventHandlerImpl.AXONFRAMEWORK_EVENTHANDLING_ANNOTATION);

        if (!isEventHandlerAnnotation(annotation)) {
            return null;
        }

        PsiAnnotationMemberValue eventType = annotation.findAttributeValue(EventHandlerImpl.EVENT_HANDLER_ARGUMENT);
        if (annotationHasEventTypeArgument(eventType) && hasChildren(eventType)) {
            return new EventHandlerImpl(annotation, eventType, method);
        }
        return new EventHandlerImpl(annotation, method);
    }

    private static boolean annotationHasEventTypeArgument(PsiAnnotationMemberValue eventType) {
        return eventType instanceof PsiExpression &&
                !((PsiExpression) eventType).getType().getCanonicalText().equals("java.lang.Class<java.lang.Void>");
    }

    private static boolean hasChildren(PsiAnnotationMemberValue eventType) {
        return eventType.getChildren().length > 0 && eventType.getFirstChild().getChildren().length > 0;
    }

    @Override
    public String toString() {
        return "EventHandler{" +
                "method=" + method +
                '}';
    }
}
