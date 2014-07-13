package org.axonframework.intellij.ide.plugin;

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.ide.util.MethodCellRenderer;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import org.axonframework.intellij.ide.plugin.handler.EventHandler;
import org.axonframework.intellij.ide.plugin.handler.EventHandlerImpl;
import org.axonframework.intellij.ide.plugin.handler.EventHandlerRepository;
import org.axonframework.intellij.ide.plugin.handler.EventHandlerRepositoryImpl;
import org.axonframework.intellij.ide.plugin.publisher.EventPublisher;
import org.axonframework.intellij.ide.plugin.publisher.EventPublisherRepository;
import org.axonframework.intellij.ide.plugin.publisher.EventPublisherRepositoryImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This class shows an icon in the gutter when an Axon annotation is found. The icon can be used to navigate to all
 * classes that handle the event.
 */
public class AxonGutterAnnotator implements Annotator {

    public static final Icon AxonIcon = IconLoader.getIcon("/icons/axon12x12.png"); // 10x14

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof PsiExpression && element.getText().contains("apply")) {
            findEventHandlers(element.getProject(), element, holder);
        }
    }

    public static void findEventHandlers(Project project, PsiElement psiElement, AnnotationHolder holder) {
        PsiSearchHelper psiSearchHelper = PsiSearchHelper.SERVICE.getInstance(project);

        AxonEventHandlerProcessor axonEventHandlerProcessor = new AxonEventHandlerProcessor(psiElement);
        psiSearchHelper.processAllFilesWithWord("EventHandler",
                GlobalSearchScope.allScope(project),
                axonEventHandlerProcessor,
                true);
        Collection<PsiElement> psiAnnotations = axonEventHandlerProcessor.getHandlerRepository().getAllHandlerPsiElements();
        createGutterIconToEventHandlers(psiElement, holder, getParentsFromPsiAnnotations(psiAnnotations));

        for (EventHandler eventHandler : axonEventHandlerProcessor.getHandlerRepository().getAllHandlers()) {
            PsiElement element = eventHandler.getPsiElement();
            if (element.getContainingFile().isEquivalentTo(psiElement.getContainingFile())) {
                createGutterIconToCommandPublishers(element, holder, axonEventHandlerProcessor.getPublisherRepository().getPublisherPsiElementsFor(eventHandler.getType()));
            }
        }
    }

    private static Set<PsiElement> getParentsFromPsiAnnotations(Collection<PsiElement> psiAnnotations) {
        Set<PsiElement> elementsToAnnotate = new HashSet<PsiElement>();
        for (PsiElement target : psiAnnotations) {
            PsiElement elementToAnnotate = target.getParent().getParent();
            elementsToAnnotate.add(elementToAnnotate);
        }
        return elementsToAnnotate;
    }

    private static void createGutterIconToCommandPublishers(PsiElement psiElement, AnnotationHolder holder, Collection<PsiElement> targets) {
        if (!targets.isEmpty()) {
            final NavigationGutterIconBuilder<PsiElement> iconBuilder =
                    NavigationGutterIconBuilder.create(AxonIcon);
            iconBuilder.setTargets(targets)
                    .setPopupTitle("Command Handlers")
                    .setCellRenderer(new MethodCellRenderer(true))
                    .setTooltipText("The list of command handlers for this event")
                    .install(holder, psiElement);
        }
    }

    private static void createGutterIconToEventHandlers(PsiElement psiElement, AnnotationHolder holder,
                                                        Collection<PsiElement> targets) {
        if (!targets.isEmpty()) {
            final NavigationGutterIconBuilder<PsiElement> iconBuilder =
                    NavigationGutterIconBuilder.create(AxonIcon);
            iconBuilder.setTargets(targets)
                    .setPopupTitle("Event Handlers")
                    .setCellRenderer(new MethodCellRenderer(true))
                    .setTooltipText("The list of event handlers for this event")
                    .install(holder, psiElement);
        }
    }


    private static class AxonEventHandlerProcessor implements Processor<PsiFile> {

        private final PsiElement psiElement;
        private EventPublisherRepository publisherRepository = new EventPublisherRepositoryImpl();
        private EventHandlerRepository handlerRepository = new EventHandlerRepositoryImpl();

        public AxonEventHandlerProcessor(PsiElement psiElement) {
            this.psiElement = psiElement;
        }

        @Override
        public boolean process(PsiFile psiFile) {
            Collection<PsiAnnotation> parameterList = PsiTreeUtil.findChildrenOfType(psiFile.getNode().getPsi(),
                    PsiAnnotation.class);
            for (PsiAnnotation psiAnnotation : parameterList) {
                if (EventHandlerImpl.isEventHandlerAnnotation(psiAnnotation)) {
                    ExtractCommandMethodArgumentVisitor commandHandlerVisitor = new ExtractCommandMethodArgumentVisitor();
                    psiElement.accept(commandHandlerVisitor);
                    ExtractEventMethodArgumentVisitor eventHandlerVisitor = new ExtractEventMethodArgumentVisitor();
                    psiAnnotation.getParent().getParent().accept(eventHandlerVisitor);

                    EventPublisher eventPublisher = commandHandlerVisitor.getEventPublisher();
                    EventHandler eventHandler = eventHandlerVisitor.getEventHandler();
                    if (commandHandlerVisitor.hasEventPublisher() && eventPublisher.canPublishType(eventHandler.getType())) {
                        handlerRepository.addHandlerForType(eventHandler.getType(), eventHandler);
                        publisherRepository.addPublisherForType(eventHandler.getType(), eventPublisher);
                    }
                }
            }
            return true;
        }

        public EventPublisherRepository getPublisherRepository() {
            return publisherRepository;
        }

        public EventHandlerRepository getHandlerRepository() {
            return handlerRepository;
        }
    }
}
