package org.axonframework.intellij.ide.plugin;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiType;
import org.axonframework.eventhandling.annotation.EventHandler;

/**
 */
public class ExtractEventMethodArgumentVisitor extends JavaRecursiveElementVisitor {

    public static final String AXONFRAMEWORK_EVENTHANDLING_ANNOTATION = EventHandler.class.getCanonicalName();
    public static final String EVENT_HANDLER_ARGUMENT = "eventType";

    private PsiType[] argument = new PsiType[]{};

    @Override
    public void visitMethod(PsiMethod method) {
        PsiAnnotation annotation = method.getModifierList().findAnnotation(AXONFRAMEWORK_EVENTHANDLING_ANNOTATION);
        if (annotation != null) {
            PsiAnnotationMemberValue eventType = annotation.findAttributeValue(EVENT_HANDLER_ARGUMENT);
            if (eventType != null) {
                if (hasChildren(eventType)) {
                    // TODO: convert to type and prefer the annotation argument instead of the function argument
                    String classNameOfEventTypeArgument = eventType.getFirstChild().getFirstChild().getReference().getCanonicalText();
                }
            }
            PsiParameterList list = method.getParameterList();
            argument = new PsiType[list.getParametersCount()];
            for (int i = 0; i < list.getParameters().length; i++) {
                PsiParameter psiParameter = list.getParameters()[i];
                argument[i] = psiParameter.getType();
            }
        }

        super.visitMethod(method);
    }

    private boolean hasChildren(PsiAnnotationMemberValue eventType) {
        return eventType.getChildren().length > 0 && eventType.getFirstChild().getChildren().length > 0;
    }


    public PsiType[] getArguments() {
        return argument;
    }
}
