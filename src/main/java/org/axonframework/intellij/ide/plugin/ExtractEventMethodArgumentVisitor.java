package org.axonframework.intellij.ide.plugin;

import com.intellij.psi.*;

/**
 */
public class ExtractEventMethodArgumentVisitor extends JavaRecursiveElementVisitor {

    public static final String AXONFRAMEWORK_EVENTHANDLING_ANNOTATION = "org.axonframework.eventhandling.annotation.EventHandler";
    public static final String EVENT_HANDLER_ARGUMENT = "eventType";

    private PsiType[] argument = new PsiType[]{};

    @Override
    public void visitMethod(PsiMethod method) {
        PsiAnnotation annotation = method.getModifierList().findAnnotation(AXONFRAMEWORK_EVENTHANDLING_ANNOTATION);
        if (methodHasAnnotation(annotation)) {
            PsiAnnotationMemberValue eventType = annotation.findAttributeValue(EVENT_HANDLER_ARGUMENT);
            if (annotationHasEventTypeArgument(eventType) && hasChildren(eventType)) {
                argument = getAnnotationArguments(eventType);
            } else {
                argument = getMethodArguments(method);
            }
        }
        super.visitMethod(method);
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

    private boolean methodHasAnnotation(PsiAnnotation annotation) {
        return annotation != null;
    }

    private boolean annotationHasEventTypeArgument(PsiAnnotationMemberValue eventType) {
        return eventType instanceof PsiExpression &&
                !((PsiExpression) eventType).getType().getCanonicalText().equals("java.lang.Class<java.lang.Void>");
    }

    private boolean hasChildren(PsiAnnotationMemberValue eventType) {
        return eventType.getChildren().length > 0 && eventType.getFirstChild().getChildren().length > 0;
    }


    public PsiType[] getArguments() {
        return argument;
    }
}
