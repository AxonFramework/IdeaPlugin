package org.axonframework.intellij.ide.plugin;

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.ide.util.MethodCellRenderer;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
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
        if (element instanceof PsiAnnotation && CommandHandler.AXONFRAMEWORK_COMMANDHANDLING_ANNOTATION.equals(((PsiAnnotation) element).getQualifiedName())) {
            PsiElement elementsUnderAnnotation = element.getParent().getParent();

            for (PsiElement elementUnderAnnotation : elementsUnderAnnotation.getChildren()) {
                for (PsiElement psiElement : elementUnderAnnotation.getChildren()) {
                    findEventHandlers(element.getProject(), psiElement, holder);
                }
            }
        }
    }

    public static void findEventHandlers(Project project, PsiElement psiElement, AnnotationHolder holder) {
        PsiSearchHelper psiSearchHelper = PsiSearchHelper.SERVICE.getInstance(project);

        AxonEventHandlerProcessor axonEventHandlerProcessor = new AxonEventHandlerProcessor(psiElement);
        psiSearchHelper.processAllFilesWithWord("EventHandler",
                GlobalSearchScope.allScope(project),
                axonEventHandlerProcessor,
                true);
        Collection<PsiElement> psiAnnotations = axonEventHandlerProcessor.getTargetLocations();
        createGutterIcon(psiElement, holder, psiAnnotations);
    }

    private static void createGutterIcon(PsiElement psiElement, AnnotationHolder holder,
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
        private Set<PsiElement> targetLocations = new HashSet<PsiElement>();

        public AxonEventHandlerProcessor(PsiElement psiElement) {
            this.psiElement = psiElement;
        }

        @Override
        public boolean process(PsiFile psiFile) {
            Collection<PsiAnnotation> parameterList = PsiTreeUtil.findChildrenOfType(psiFile.getNode().getPsi(),
                    PsiAnnotation.class);
            for (PsiAnnotation psiAnnotation : parameterList) {
                if (EventHandler.isEventHandlerAnnotation(psiAnnotation)) {
                    ExtractCommandMethodArgumentVisitor commandHandlerVisitor = new ExtractCommandMethodArgumentVisitor();
                    psiElement.accept(commandHandlerVisitor);
                    ExtractEventMethodArgumentVisitor eventHandlerVisitor = new ExtractEventMethodArgumentVisitor();
                    psiAnnotation.getParent().getParent().accept(eventHandlerVisitor);

                    CommandHandler commandHandler = commandHandlerVisitor.getCommandHandler();
                    EventHandler eventHandler = eventHandlerVisitor.getEventHandler();
                    if (commandHandlerVisitor.hasCommandHandler() && commandHandler.commandCanHandleEvent(eventHandler)) {
                        targetLocations.add(psiAnnotation.getParent().getParent());
                    }
                }
            }
            return true;
        }

        public Set<PsiElement> getTargetLocations() {
            return targetLocations;
        }
    }
}
