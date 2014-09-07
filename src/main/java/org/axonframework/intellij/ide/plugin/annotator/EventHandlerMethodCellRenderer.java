package org.axonframework.intellij.ide.plugin.annotator;

import com.intellij.ide.util.MethodCellRenderer;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import org.axonframework.intellij.ide.plugin.eventhandler.EventHandler;
import org.axonframework.intellij.ide.plugin.eventhandler.HandlerProviderManager;
import org.axonframework.intellij.ide.plugin.eventhandler.IsMethodCondition;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class EventHandlerMethodCellRenderer extends PsiElementListCellRenderer {

    private static final Icon AxonInternalEvent = IconLoader.getIcon("/icons/aggregate_event.png");
    private static final Icon AxonSagaEvent = IconLoader.getIcon("/icons/saga_event.png");
    private static final Icon AxonEvent = IconLoader.getIcon("/icons/plain_event.png");

    private final IconMethodCellRenderer delegate;

    public EventHandlerMethodCellRenderer(HandlerProviderManager handlerProviderManager) {
        this.delegate = new IconMethodCellRenderer(handlerProviderManager);
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

        private final HandlerProviderManager handlerProviderManager;

        public IconMethodCellRenderer(HandlerProviderManager handlerProviderManager) {
            super(true);
            this.handlerProviderManager = handlerProviderManager;
        }

        @Override
        public Icon getIcon(PsiElement element) {
            EventHandler eventHandler = handlerProviderManager.resolveEventHandler(element);
            if (eventHandler == null) {
                return super.getIcon(element);
            }
            if (eventHandler.isInternalEvent()) {
                return AxonInternalEvent;
            }
            if (eventHandler.isSagaEvent()) {
                return AxonSagaEvent;
            }
            return AxonEvent;
        }
    }
}
