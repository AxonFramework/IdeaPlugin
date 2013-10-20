package org.axonframework.ide.intellij.inspection;

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
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
import com.intellij.util.NotNullFunction;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;

/**
 * This class shows an icon in the gutter when an Axon annotation is found. The icon can be used to navigate to all
 * classes that handle the event.
 */
public class AxonGutterAnnotator implements Annotator {

    public static final Icon AxonIcon = IconLoader.getIcon("/icons/axon12x12.png"); // 10x14

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof PsiAnnotation && element.textMatches("@CommandHandler")) {
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

        AxonEventHandlerProcessor axonEventHandlerProcessor = new AxonEventHandlerProcessor(psiElement, holder);
        psiSearchHelper.processAllFilesWithWord("EventHandler", GlobalSearchScope.allScope(project), axonEventHandlerProcessor, true);
    }


    private static class PsiElementConverter implements NotNullFunction<PsiElement, Collection<? extends PsiElement>> {
        public static final PsiElementConverter INSTANCE = new PsiElementConverter();

        @NotNull
        public Collection<? extends PsiElement> fun(final PsiElement pointer) {
            return Collections.singleton(pointer);
        }
    }

    private static class AxonEventHandlerProcessor implements Processor<PsiFile> {

        private final PsiElement psiElement;
        private AnnotationHolder holder;

        public AxonEventHandlerProcessor(PsiElement psiElement, AnnotationHolder holder) {
            this.psiElement = psiElement;
            this.holder = holder;
        }

        @Override
        public boolean process(PsiFile psiFile) {
            List<PsiAnnotation> annotations = new ArrayList<PsiAnnotation>();

            Collection<PsiAnnotation> parameterList = PsiTreeUtil.findChildrenOfAnyType(psiFile.getNode().getPsi(), PsiAnnotation.class);
            if (parameterList != null) {
                for (PsiAnnotation psiAnnotation : parameterList) {
                    String typeParameters = psiAnnotation.getText();
                    if (typeParameters.contains("@EventHandler")) {
                        ExtractCommandMethodArgumentVisitor commandHandlerVisitor = new ExtractCommandMethodArgumentVisitor();
                        psiElement.accept(commandHandlerVisitor);
                        ExtractEventMethodArgumentVisitor eventHandlerVisitor = new ExtractEventMethodArgumentVisitor();
                        psiAnnotation.getParent().getParent().accept(eventHandlerVisitor);
                        if (commandHandlerVisitor.commandCanHandleArguments(eventHandlerVisitor.getArguments())) {
                            annotations.add(psiAnnotation);
                        }
                    }
                }
                annotate(annotations, psiFile);
            }

            return true;
        }

        private void annotate(List<PsiAnnotation> psiAnnotations, PsiFile psiFile) {
            final NavigationGutterIconBuilder<PsiElement> iconBuilder =
                    NavigationGutterIconBuilder.create(AxonIcon, PsiElementConverter.INSTANCE);
            iconBuilder.
                    setTargets(psiAnnotations).
                    setPopupTitle("Event Handlers").
                    setCellRenderer(new DefaultPsiElementCellRenderer()).
                    setTooltipText("The list of event handlers for this command in " + psiFile.getName()).
                    install(holder, psiElement);
        }
    }
}
