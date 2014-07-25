package org.axonframework.intellij.ide.plugin.publisher;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;

public interface EventPublisher {

    boolean canPublishType(PsiType eventType);

    PsiType getPublishedType();

    PsiElement getPsiElement();

    PsiMethod getEnclosingMethod();

    boolean isValid();
}
