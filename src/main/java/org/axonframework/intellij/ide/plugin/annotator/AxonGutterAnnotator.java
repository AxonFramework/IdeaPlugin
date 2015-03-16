package org.axonframework.intellij.ide.plugin.annotator;

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.CommonProcessors;
import org.axonframework.intellij.ide.plugin.handler.AnnotationTypes;
import org.axonframework.intellij.ide.plugin.handler.EventHandler;
import org.axonframework.intellij.ide.plugin.handler.HandlerProviderManager;
import org.axonframework.intellij.ide.plugin.publisher.EventPublisher;
import org.axonframework.intellij.ide.plugin.publisher.PublisherProviderManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;

/**
 * This class shows an icon in the gutter when an Axon annotation is found. The icon can be used to navigate to all
 * classes that handle the event.
 */
public class AxonGutterAnnotator implements Annotator {

    private static final Icon AxonIconIn = IconLoader.getIcon("/icons/axon_into.png"); // 16x16
    private static final Icon AxonIconOut = IconLoader.getIcon("/icons/axon_publish.png"); // 16x16

    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof PsiClass) {
            tryAnnotateEventClass(holder, (PsiClass) element);
            tryAnnotateCommandClass(holder, (PsiClass) element);
        }
        if (!(element instanceof PsiMethodCallExpression || element instanceof PsiIdentifier)) {
            return;
        }

        final PublisherProviderManager publisherManager = PublisherProviderManager.getInstance(element.getProject());
        final HandlerProviderManager handlerManager = HandlerProviderManager.getInstance(element.getProject());
        final EventPublisher publisher = publisherManager.resolveEventPublisher(element);
        final EventHandler handler = handlerManager.resolveEventHandler(element.getContext());
        if (publisher != null) {
            NotNullLazyValue<Collection<? extends PsiElement>> targetResolver = new NotNullLazyValue<Collection<? extends PsiElement>>() {
                @NotNull
                @Override
                protected Collection<? extends PsiElement> compute() {
                    Set<EventHandler> handlers = handlerManager.getRepository().findHandlers(publisher.getPublishedType());
                    Set<PsiEventHandlerWrapper> destinations = new TreeSet<PsiEventHandlerWrapper>();
                    for (EventHandler eventHandler : handlers) {
                        PsiElement elementForAnnotation = eventHandler.getElementForAnnotation();
                        if (elementForAnnotation.isValid()) {
                            destinations.add(new PsiEventHandlerWrapper(elementForAnnotation, eventHandler));
                        }
                    }
                    return destinations;
                }
            };
            Annotation gutterIconForPublisher = createGutterIconForEventHandlers(element, holder, targetResolver, handlerManager);
            if (targetResolver.getValue().isEmpty()) {
                addCreateEventHandlerQuickFixes(publisher, gutterIconForPublisher);
            }
        }

        if (handler != null) {
            createGutterIconForEventPublishers(handler.getElementForAnnotation(), holder, new NotNullLazyValue<Collection<? extends PsiElement>>() {
                @NotNull
                @Override
                protected Collection<? extends PsiElement> compute() {
                    Collection<EventPublisher> publishers = publisherManager.getRepository()
                            .getPublishersFor(handler.getHandledType());
                    Collection<PsiElement> publishLocations = new ArrayList<PsiElement>();
                    for (EventPublisher eventPublisher : publishers) {
                        PsiElement psiElement = eventPublisher.getPsiElement();
                        if (psiElement.isValid()) {
                            publishLocations.add(psiElement);
                        }
                    }
                    return publishLocations;
                }
            });
        }
    }

    private void tryAnnotateCommandClass(AnnotationHolder holder, PsiClass classElement) {
        final Collection<PsiMethod> commandHandlerMethods =
                findHandlerMethod(classElement.getProject(), classElement, AnnotationTypes.COMMAND_EVENT_HANDLER.getFullyQualifiedName());
        if (!commandHandlerMethods.isEmpty()) {
            createGutterIconForCommandHandlers(
                    classElement.getNameIdentifier(),
                    holder,
                    constantNotNullLazyValue(commandHandlerMethods));
        }
    }

    private void tryAnnotateEventClass(AnnotationHolder holder, PsiClass classElement) {
        List<AnnotationTypes> eventAnnotations = Arrays.asList(
                AnnotationTypes.EVENT_HANDLER,
                AnnotationTypes.EVENT_SOURCING_HANDLER,
                AnnotationTypes.SAGA_EVENT_HANDLER);
        Collection<PsiMethod> eventHandlerMethods = new ArrayList<PsiMethod>();
        for (AnnotationTypes eventAnnotation : eventAnnotations) {
            eventHandlerMethods.addAll(findHandlerMethod(classElement.getProject(), classElement, eventAnnotation.getFullyQualifiedName()));
        }
        if (!eventHandlerMethods.isEmpty()) {
            createGutterIconForEventHandlers(
                    classElement.getNameIdentifier(),
                    holder,
                    constantNotNullLazyValue(eventHandlerMethods),
                    HandlerProviderManager.getInstance(classElement.getProject()));
        }
    }

    public Collection<PsiMethod> findHandlerMethod(Project project, final PsiClass clazz, String handlerAnnotation) {
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        PsiClass handlerClass =
                JavaPsiFacade.getInstance(project).findClass(handlerAnnotation, scope);
        CommonProcessors.CollectProcessor<PsiMethod> collectProcessor = new CommonProcessors.CollectProcessor<PsiMethod>() {
            @Override
            protected boolean accept(PsiMethod psiMethod) {
                return psiMethod.getParameterList().getParametersCount() > 0
                        && clazz.isEquivalentTo(PsiUtil.resolveClassInType(psiMethod.getParameterList().getParameters()[0].getType()));
            }
        };
        AnnotatedElementsSearch.searchPsiMethods(handlerClass, scope).forEach(collectProcessor);
        return collectProcessor.getResults();
    }

    private NotNullLazyValue<Collection<? extends PsiElement>> constantNotNullLazyValue(final Collection<? extends PsiElement> psiElements) {
        return new NotNullLazyValue<Collection<? extends PsiElement>>() {
            @NotNull
            @Override
            protected Collection<? extends PsiElement> compute() {
                return psiElements;
            }
        };
    }

    private void addCreateEventHandlerQuickFixes(EventPublisher publisher, Annotation gutterIconForPublisher) {
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
                                                               NotNullLazyValue<Collection<? extends PsiElement>> targetResolver, HandlerProviderManager handlerManager) {
        return NavigationGutterIconBuilder.create(AxonIconOut)
                .setEmptyPopupText("No handlers found for this event")
                .setTargets(targetResolver)
                .setPopupTitle("Event Handlers")
                .setCellRenderer(new EventHandlerMethodCellRenderer(handlerManager))
                .setTooltipText("Navigate to the handlers")
                .install(holder, psiElement);
    }

}
