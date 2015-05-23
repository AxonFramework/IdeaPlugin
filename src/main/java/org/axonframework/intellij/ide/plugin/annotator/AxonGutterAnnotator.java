package org.axonframework.intellij.ide.plugin.annotator;

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.util.PsiTypesUtil;
import org.axonframework.intellij.ide.plugin.handler.AnnotationTypes;
import org.axonframework.intellij.ide.plugin.handler.Handler;
import org.axonframework.intellij.ide.plugin.handler.HandlerProviderManager;
import org.axonframework.intellij.ide.plugin.publisher.Publisher;
import org.axonframework.intellij.ide.plugin.publisher.PublisherProviderManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class shows an icon in the gutter when an Axon annotation is found. The icon can be used to navigate to all
 * classes that handle the event.
 */
public class AxonGutterAnnotator implements Annotator {

    private static final Icon AxonIconIn = IconLoader.getIcon("/icons/axon_into.png"); // 16x16
    private static final Icon AxonIconOut = IconLoader.getIcon("/icons/axon_publish.png"); // 16x16

    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        final PublisherProviderManager publisherManager = PublisherProviderManager.getInstance(element.getProject());
        final HandlerProviderManager handlerManager = HandlerProviderManager.getInstance(element.getProject());
        if (element instanceof PsiClass) {
            tryAnnotateEventClass(holder, (PsiClass) element, publisherManager, handlerManager);
            tryAnnotateCommandClass(holder, (PsiClass) element, handlerManager);
        } else if ((element instanceof PsiMethodCallExpression || element instanceof PsiIdentifier)) {
            final Publisher publisher = publisherManager.resolveEventPublisher(element);
            final Handler handler = handlerManager.resolveEventHandler(element.getContext());
            if (publisher != null) {
                NotNullLazyValue<Collection<? extends PsiElement>> targetResolver =
                        createNotNullLazyValueForHandlers(handlerManager.getRepository().findHandlers(publisher.getPublishedType()));
                Annotation gutterIconForPublisher = createGutterIconForEventHandlers(element, holder, targetResolver, handlerManager);
                if (targetResolver.getValue().isEmpty()) {
                    addCreateEventHandlerQuickFixes(publisher, gutterIconForPublisher);
                }
            }

            if (handler != null) {
                Collection<Publisher> publishers = publisherManager.getRepository()
                        .getPublishersFor(handler.getHandledType());
                createGutterIconForEventPublishers(
                        handler.getElementForAnnotation(),
                        holder,
                        createNotNullLazyValueForPublishers(publishers));
            }
        }
    }

    private NotNullLazyValue<Collection<? extends PsiElement>> createNotNullLazyValueForPublishers(final Collection<Publisher> publishers) {
        return new NotNullLazyValue<Collection<? extends PsiElement>>() {
            @NotNull
            @Override
            protected Collection<? extends PsiElement> compute() {
                Collection<PsiElement> publishLocations = new ArrayList<PsiElement>();
                for (Publisher eventPublisher : publishers) {
                    PsiElement psiElement = eventPublisher.getPsiElement();
                    if (psiElement.isValid()) {
                        publishLocations.add(psiElement);
                    }
                }
                return publishLocations;
            }
        };
    }

    private NotNullLazyValue<Collection<? extends PsiElement>> createNotNullLazyValueForHandlers(final Set<Handler> handlers) {
        return new NotNullLazyValue<Collection<? extends PsiElement>>() {
            @NotNull
            @Override
            protected Collection<? extends PsiElement> compute() {
                Set<PsiEventHandlerWrapper> destinations = new TreeSet<PsiEventHandlerWrapper>();
                for (Handler eventHandler : handlers) {
                    PsiElement elementForAnnotation = eventHandler.getElementForAnnotation();
                    if (elementForAnnotation.isValid()) {
                        destinations.add(new PsiEventHandlerWrapper(elementForAnnotation, eventHandler));
                    }
                }
                return destinations;
            }
        };
    }

    private void tryAnnotateCommandClass(AnnotationHolder holder, PsiClass classElement, HandlerProviderManager handlerManager) {
        Set<Handler> handlers =
                handlerManager.getRepository().findCommandHandlers(PsiTypesUtil.getClassType(classElement));
        if (!handlers.isEmpty()) {
            createGutterIconForCommandHandlers(
                    classElement.getNameIdentifier(),
                    holder,
                    createNotNullLazyValueForHandlers(handlers));
        }
    }

    private void tryAnnotateEventClass(AnnotationHolder holder, PsiClass classElement,
                                       PublisherProviderManager publisherManager, HandlerProviderManager handlerManager) {
        Set<Publisher> publishers = publisherManager.getRepository().getPublishersFor(PsiTypesUtil.getClassType(classElement));
        if (!publishers.isEmpty()) {
            createGutterIconForEventPublishers(
                    classElement.getNameIdentifier(),
                    holder,
                    createNotNullLazyValueForPublishers(publishers));
        }
        Set<Handler> handlers =
                handlerManager.getRepository().findEventHandlers(PsiTypesUtil.getClassType(classElement));
        if (!handlers.isEmpty()) {
            createGutterIconForEventHandlers(
                    classElement.getNameIdentifier(),
                    holder,
                    createNotNullLazyValueForHandlers(handlers),
                    handlerManager);
        }
    }

    private void addCreateEventHandlerQuickFixes(Publisher publisher, Annotation gutterIconForPublisher) {
        gutterIconForPublisher.registerFix(new CreateEventHandlerQuickfix(publisher.getPublishedType(), AnnotationTypes.EVENT_HANDLER));
        gutterIconForPublisher.registerFix(new CreateEventHandlerQuickfix(publisher.getPublishedType(), AnnotationTypes.EVENT_SOURCING_HANDLER));
        gutterIconForPublisher.registerFix(new CreateEventHandlerQuickfix(publisher.getPublishedType(), AnnotationTypes.SAGA_EVENT_HANDLER));
    }

    private static Annotation createGutterIconForEventPublishers(PsiElement psiElement, AnnotationHolder holder,
                                                                 NotNullLazyValue<Collection<? extends PsiElement>> targetResolver) {
        return NavigationGutterIconBuilder.create(AxonIconIn)
                .setEmptyPopupText("No publishers found for this event")
                .setTargets(targetResolver)
                .setPopupTitle("Publishers")
                .setCellRenderer(new ContainingMethodCellRenderer())
                .setTooltipText("Navigate to the publishers")
                .install(holder, psiElement);
    }

    private static Annotation createGutterIconForCommandHandlers(PsiElement psiElement, AnnotationHolder holder,
                                                                 NotNullLazyValue<Collection<? extends PsiElement>> targetResolver) {
        return NavigationGutterIconBuilder.create(AxonIconIn)
                .setEmptyPopupText("No handlers found for this command")
                .setTargets(targetResolver)
                .setPopupTitle("Command handlers")
                .setCellRenderer(new ContainingMethodCellRenderer())
                .setTooltipText("Navigate to the handlers")
                .install(holder, psiElement);
    }

    private static Annotation createGutterIconForEventHandlers(PsiElement psiElement, AnnotationHolder holder,
                                                               NotNullLazyValue<Collection<? extends PsiElement>> targetResolver,
                                                               HandlerProviderManager handlerManager) {
        return NavigationGutterIconBuilder.create(AxonIconOut)
                .setEmptyPopupText("No handlers found for this event")
                .setTargets(targetResolver)
                .setPopupTitle("Event Handlers")
                .setCellRenderer(new EventHandlerMethodCellRenderer(handlerManager))
                .setTooltipText("Navigate to the handlers")
                .install(holder, psiElement);
    }

}
