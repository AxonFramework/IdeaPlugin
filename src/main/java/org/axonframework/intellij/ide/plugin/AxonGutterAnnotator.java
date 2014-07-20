package org.axonframework.intellij.ide.plugin;

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.ide.util.MethodCellRenderer;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import org.axonframework.intellij.ide.plugin.handler.*;
import org.axonframework.intellij.ide.plugin.publisher.AxonEventPublisherProcessor;
import org.axonframework.intellij.ide.plugin.publisher.EventPublisher;
import org.axonframework.intellij.ide.plugin.publisher.EventPublisherRepository;
import org.axonframework.intellij.ide.plugin.publisher.EventPublisherRepositoryImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This class shows an icon in the gutter when an Axon annotation is found. The icon can be used to navigate to all
 * classes that handle the event.
 */
public class AxonGutterAnnotator implements Annotator {

    public static final Icon AxonIconIn = IconLoader.getIcon("/icons/axon_into.png"); // 10x14
    public static final Icon AxonIconOut = IconLoader.getIcon("/icons/axon_publish.png"); // 10x14

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        PsiSearchHelper psiSearchHelper = PsiSearchHelper.SERVICE.getInstance(element.getProject());
        EventHandlerRepository eventHandlerRepository = new EventHandlerRepositoryImpl();
        EventPublisherRepository eventPublisherRepository = new EventPublisherRepositoryImpl();

        if (isPublishing(element)) {
            AxonEventProcessor axonEventHandlerProcessor = new AxonEventHandlerProcessor(element,
                    eventPublisherRepository, eventHandlerRepository);

            psiSearchHelper.processAllFilesWithWord("EventHandler",
                    GlobalSearchScope.allScope(element.getProject()),
                    axonEventHandlerProcessor,
                    true);

            findEventHandlers(element, holder, axonEventHandlerProcessor);
        } else if (isHandling(element)) {
            AxonEventProcessor axonEventPublisherProcessor = new AxonEventPublisherProcessor(element,
                    eventPublisherRepository, eventHandlerRepository);

            psiSearchHelper.processAllFilesWithWord("apply",
                    GlobalSearchScope.allScope(element.getProject()),
                    axonEventPublisherProcessor,
                    true);

            findEventPublishers(element, holder, axonEventPublisherProcessor);
        }
    }

    private boolean isHandling(PsiElement element) {
        return element instanceof PsiAnnotation && EventHandlerImpl.isEventHandlerAnnotation((PsiAnnotation) element);
    }

    private boolean isPublishing(PsiElement element) {
        return element instanceof PsiExpression && element.getText().contains("apply");
    }

    public static void findEventHandlers(PsiElement psiElement, AnnotationHolder holder, AxonEventProcessor axonEventHandlerProcessor) {
        Collection<PsiMethod> psiMethods = axonEventHandlerProcessor.getHandlerRepository()
                .getAllHandlerPsiElements();
        createGutterIconToEventHandlers(psiElement, holder, psiMethods);
    }

    private static void findEventPublishers(PsiElement psiHandler, AnnotationHolder holder, AxonEventProcessor axonEventHandlerProcessor) {
        Collection<EventPublisher> allPublishers = axonEventHandlerProcessor.getPublisherRepository().getAllPublishers();
        Collection<PsiElement> publishers = new ArrayList<PsiElement>();
        EventHandler eventHandler = createEventHandlerFrom(psiHandler);
        for (EventPublisher eventPublisher : allPublishers) {
            if (eventPublisher.canPublishType(eventHandler.getHandledType())) {
                publishers.add(eventPublisher.getPsiElement());
            }
        }
        createGutterIconToEventPublishers(psiHandler, holder, publishers);
    }

    private static EventHandler createEventHandlerFrom(PsiElement psiHandler) {
        ExtractEventHandlerArgumentVisitor eventHandlerVisitor = new ExtractEventHandlerArgumentVisitor();
        psiHandler.getParent().getParent().accept(eventHandlerVisitor);
        return eventHandlerVisitor.getEventHandler();
    }

    private static void createGutterIconToEventPublishers(PsiElement psiElement, AnnotationHolder holder,
                                                          Collection<PsiElement> targets) {
        if (!targets.isEmpty()) {
            NavigationGutterIconBuilder.create(AxonIconIn)
                    .setTargets(targets)
                    .setPopupTitle("Publishers")
                    .setCellRenderer(new MethodCellRenderer(true))
                    .setTooltipText("Navigate to the publishers of this event")
                    .install(holder, psiElement);
        }
    }

    private static void createGutterIconToEventHandlers(PsiElement psiElement, AnnotationHolder holder,
                                                        Collection<? extends PsiElement> targets) {
        if (!targets.isEmpty()) {
            NavigationGutterIconBuilder.create(AxonIconOut)
                    .setTargets(targets)
                    .setPopupTitle("Event Handlers")
                    .setCellRenderer(new MethodCellRenderer(true))
                    .setTooltipText("Navigate to the handlers for this event")
                    .install(holder, psiElement);
        }
    }

}
