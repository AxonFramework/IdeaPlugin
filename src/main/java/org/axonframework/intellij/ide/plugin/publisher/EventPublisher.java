package org.axonframework.intellij.ide.plugin.publisher;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.Nullable;

public interface EventPublisher {

    boolean canPublishType(PsiType eventType);

    PsiType getPublishedType();

    PsiElement getPsiElement();

    boolean isValid();

    @Nullable PsiClass getEnclosingClass();
}
