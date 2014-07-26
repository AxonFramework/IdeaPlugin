package org.axonframework.intellij.ide.plugin.handler;

import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

public class IsMethodCondition implements Condition<PsiElement> {

    @Override
    public boolean value(PsiElement psiElement) {
        return psiElement instanceof PsiMethod;
    }
}
