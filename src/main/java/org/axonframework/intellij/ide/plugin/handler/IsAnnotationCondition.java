package org.axonframework.intellij.ide.plugin.handler;

import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;

public class IsAnnotationCondition implements Condition<PsiElement> {

    @Override
    public boolean value(PsiElement psiElement) {
        return psiElement instanceof PsiAnnotation;
    }
}
