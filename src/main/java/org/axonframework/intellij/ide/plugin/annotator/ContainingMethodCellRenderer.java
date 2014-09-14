package org.axonframework.intellij.ide.plugin.annotator;

import com.intellij.ide.util.MethodCellRenderer;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import org.axonframework.intellij.ide.plugin.handler.IsMethodCondition;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

class ContainingMethodCellRenderer extends PsiElementListCellRenderer<PsiElement> {

    private final IconMethodCellRenderer delegate;

    ContainingMethodCellRenderer() {
        this.delegate = new IconMethodCellRenderer();
    }

    @Override
    public String getElementText(PsiElement psiElement) {
        return delegate.getElementText(enclosingMethodOf(psiElement));
    }

    @Nullable
    @Override
    protected String getContainerText(PsiElement psiElement, String name) {
        return delegate.getContainerText(enclosingMethodOf(psiElement), name);
    }

    private PsiMethod enclosingMethodOf(PsiElement psiElement) {
        return (PsiMethod) PsiTreeUtil.findFirstParent(psiElement, new IsMethodCondition());
    }

    @Override
    protected int getIconFlags() {
        return delegate.getIconFlags();
    }

    @Override
    protected Icon getIcon(PsiElement element) {
        return delegate.getIcon(enclosingMethodOf(element));
    }

    private static class IconMethodCellRenderer extends MethodCellRenderer {

        public IconMethodCellRenderer() {
            super(true);
        }

        @Override
        public Icon getIcon(PsiElement element) {
            return super.getIcon(element);
        }
    }
}
