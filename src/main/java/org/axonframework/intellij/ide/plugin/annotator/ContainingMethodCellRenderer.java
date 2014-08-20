package org.axonframework.intellij.ide.plugin.annotator;

import com.intellij.ide.util.MethodCellRenderer;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ui.UIUtil;
import org.axonframework.intellij.ide.plugin.handler.EventHandler;
import org.axonframework.intellij.ide.plugin.handler.IsMethodCondition;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

import static org.axonframework.intellij.ide.plugin.annotator.AxonGutterAnnotator.AxonIconIn;

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

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component listCellRendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof PsiEventHandlerWrapper) {
            EventHandler eventHandler = ((PsiEventHandlerWrapper) value).getEventHandler();

            if (eventHandler.isInternalEvent()) {
                PsiClass enclosingClass = eventHandler.getEnclosingClass();
                final JLabel label = new JLabel(enclosingClass != null ? enclosingClass.getName() : "", AxonIconIn, JLabel.LEFT);
                label.setBackground(UIUtil.getListBackground());

                label.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                add(label, BorderLayout.EAST);
            }

        }

        return listCellRendererComponent;
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
