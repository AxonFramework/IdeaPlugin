package org.axonframework.intellij.ide.plugin;

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.ide.util.MethodCellRenderer;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import org.axonframework.intellij.ide.plugin.handler.EventHandler;
import org.axonframework.intellij.ide.plugin.handler.HandlerProviderManager;
import org.axonframework.intellij.ide.plugin.publisher.EventPublisher;
import org.axonframework.intellij.ide.plugin.publisher.PublisherProviderManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;

/**
 * This class shows an icon in the gutter when an Axon annotation is found. The icon can be used to navigate to all
 * classes that handle the event.
 */
public class AxonGutterAnnotator implements Annotator {

    public static final Icon AxonIconIn = IconLoader.getIcon("/icons/axon_into.png"); // 16x16
    public static final Icon AxonIconOut = IconLoader.getIcon("/icons/axon_publish.png"); // 16x16

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        final PublisherProviderManager publisherManager = PublisherProviderManager.getInstance(element.getProject());
        final HandlerProviderManager handlerManager = HandlerProviderManager.getInstance(element.getProject());
        final EventPublisher publisher = publisherManager.resolveEventPublisher(element);
        final EventHandler handler = handlerManager.resolveEventHandler(element);
        if (publisher != null) {
            createGutterIconForPublisher(element, holder, new NotNullLazyValue<Collection<? extends PsiElement>>() {
                @NotNull
                @Override
                protected Collection<? extends PsiElement> compute() {
                    Set<EventHandler> handlers = handlerManager.getRepository()
                                                               .findHandlers(publisher.getPublishedType());
                    Collection<PsiElement> destinations = new HashSet<PsiElement>();
                    for (EventHandler eventHandler : handlers) {
                        destinations.add(eventHandler.getElementForAnnotation());
                    }
                    return destinations;
                }
            });
        }

        if (handler != null) {
            createGutterIconForHandler(element, holder, new NotNullLazyValue<Collection<? extends PsiElement>>() {
                @NotNull
                @Override
                protected Collection<? extends PsiElement> compute() {
                    Collection<EventPublisher> publishers = publisherManager.getRepository()
                                                                            .getPublishersFor(handler.getHandledType());
                    Collection<PsiElement> publishLocations = new ArrayList<PsiElement>();
                    for (EventPublisher eventPublisher : publishers) {
                        publishLocations.add(eventPublisher.getPsiElement());
                    }
                    return publishLocations;
                }
            });
        }
    }

    private static void createGutterIconForHandler(PsiElement psiElement, AnnotationHolder holder,
                                                   NotNullLazyValue<Collection<? extends PsiElement>> targetResolver) {
        NavigationGutterIconBuilder.create(AxonIconIn)
                                   .setEmptyPopupText("No publishers found for this event")
                                   .setTargets(targetResolver)
                                   .setPopupTitle("Publishers")
                                   .setCellRenderer(new ContainingMethodCellRenderer())
                                   .setTooltipText("Navigate to the publishers of this event")
                                   .install(holder, psiElement);
    }

    private static void createGutterIconForPublisher(PsiElement psiElement, AnnotationHolder holder,
                                                     NotNullLazyValue<Collection<? extends PsiElement>> targetResolver) {
        NavigationGutterIconBuilder.create(AxonIconOut)
                                   .setEmptyPopupText("No handlers found for this event")
                                   .setTargets(targetResolver)
                                   .setPopupTitle("Event Handlers")
                                   .setCellRenderer(new ContainingMethodCellRenderer())
                                   .setTooltipText("Navigate to the handlers for this event")
                                   .install(holder, psiElement);
    }

    private static class ContainingMethodCellRenderer
            extends com.intellij.ide.util.PsiElementListCellRenderer<PsiElement> {

        private final IconMethodCellRenderer delegate;

        private ContainingMethodCellRenderer() {
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
}
