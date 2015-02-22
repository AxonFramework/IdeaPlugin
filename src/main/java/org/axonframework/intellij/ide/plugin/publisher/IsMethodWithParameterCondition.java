package org.axonframework.intellij.ide.plugin.publisher;

import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

public class IsMethodWithParameterCondition implements Condition<PsiElement> {
    @Override
    public boolean value(PsiElement psiElement) {
        boolean isMethod = psiElement instanceof PsiMethod;
        if (isMethod) {
            PsiMethod method = (PsiMethod) psiElement;
            return method.getParameterList().getChildren().length > 0;
        }
        return false;
    }
}
