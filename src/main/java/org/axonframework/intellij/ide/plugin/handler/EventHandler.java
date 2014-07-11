package org.axonframework.intellij.ide.plugin.handler;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;

public interface EventHandler {

    PsiType getType();

    PsiElement getPsiElement();
}
