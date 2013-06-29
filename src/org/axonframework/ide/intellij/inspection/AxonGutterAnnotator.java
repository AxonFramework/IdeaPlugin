package org.axonframework.ide.intellij.inspection;

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.NotNullFunction;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
                    if (psiElement.getText().contains("apply")) {
                        final NavigationGutterIconBuilder<PsiElement> iconBuilder =
                                NavigationGutterIconBuilder.create(AxonIcon, PsiElementConverter.INSTANCE);
                        iconBuilder.
                                setTargets(findMethods(element.getProject())).
                                setPopupTitle("Event Handlers").
                                setCellRenderer(new DefaultPsiElementCellRenderer()).
                                setTooltipText("The list of event handlers for this command").
                                install(holder, psiElement);
                        return;
                    }
                }
            }
        }
    }

    public static List<PsiElement> findMethods(Project project) {
        List<PsiElement> eventHandlers = new ArrayList<PsiElement>();
        Collection<VirtualFile> virtualFiles = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, JavaFileType.INSTANCE,
                GlobalSearchScope.projectScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);
            if (file != null) {
                Collection<PsiAnnotation> parameterList = PsiTreeUtil.findChildrenOfAnyType(file.getNode().getPsi(), PsiAnnotation.class);
                if (parameterList != null) {
                    for (PsiAnnotation psiAnnotation : parameterList) {
                        String typeParameters = psiAnnotation.getText();
                        if (typeParameters.contains("@EventHandler")) {
                            eventHandlers.add(psiAnnotation);
                        }
                    }
                }
            }
        }
        return eventHandlers;
    }


    private static class PsiElementConverter implements NotNullFunction<PsiElement, Collection<? extends PsiElement>> {
        public static final PsiElementConverter INSTANCE = new PsiElementConverter();

        @NotNull
        public Collection<? extends PsiElement> fun(final PsiElement pointer) {
            return Collections.singleton(pointer);
        }
    }

}
