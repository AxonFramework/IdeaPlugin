package org.axonframework.intellij.ide.plugin;

import com.intellij.psi.*;

/**
 */
public class ExtractEventMethodArgumentVisitor extends JavaRecursiveElementVisitor {

    private EventHandler eventHandler;

    @Override
    public void visitMethod(PsiMethod method) {
        PsiAnnotation annotation = method.getModifierList().findAnnotation(EventHandler.AXONFRAMEWORK_EVENTHANDLING_ANNOTATION);
        if (methodHasAnnotation(annotation)) {
            PsiAnnotationMemberValue eventType = annotation.findAttributeValue(EventHandler.EVENT_HANDLER_ARGUMENT);
            if (annotationHasEventTypeArgument(eventType) && hasChildren(eventType)) {
                eventHandler = new EventHandler(eventType);
            } else {
                eventHandler = new EventHandler(method);
            }
        }
        super.visitMethod(method);
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

    public EventHandler getEventHandler() {
        return eventHandler;
    }
}
