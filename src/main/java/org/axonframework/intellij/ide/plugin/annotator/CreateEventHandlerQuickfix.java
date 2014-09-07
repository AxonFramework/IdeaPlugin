package org.axonframework.intellij.ide.plugin.annotator;

import com.intellij.codeInsight.daemon.impl.quickfix.CreateMethodQuickFix;
import com.intellij.codeInspection.IntentionAndQuickFixAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.impl.source.codeStyle.ImportHelper;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.axonframework.intellij.ide.plugin.eventhandler.EventAnnotationTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

class CreateEventHandlerQuickfix extends IntentionAndQuickFixAction {

    private final PsiType type;
    private final EventAnnotationTypes eventHandler;

    public CreateEventHandlerQuickfix(PsiType type, EventAnnotationTypes eventHandler) {
        this.type = type;
        this.eventHandler = eventHandler;
    }

    @NotNull
    @Override
    public String getName() {
        return "Create new " + eventHandler.getAnnotation() + " for " + type.getPresentableText();
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Axon Framework";
    }

    @Override
    public void applyFix(@NotNull Project project, PsiFile file, @Nullable Editor editor) {
        Collection<PsiClass> classesInFile = PsiTreeUtil.findChildrenOfType(file, PsiClass.class);
        if (classesInFile.size() > 0) {
            PsiClass psiClass = classesInFile.iterator().next();

            PsiClass classType = JavaPsiFacade.getInstance(project)
                    .findClass(type.getCanonicalText(), GlobalSearchScope.allScope(project));
            String requiredProperty = getRequiredProperty(classType);

            CreateMethodQuickFix fix = CreateMethodQuickFix.createFix(psiClass,
                    eventHandler.getAnnotation() + requiredProperty + "\npublic void eventHandler(" + type.getPresentableText() + " eventType)", "");
            if (fix != null) {
                fix.applyFix();
            }

            PsiClass eventHandlerAnnotation = JavaPsiFacade.getInstance(project)
                    .findClass(eventHandler.getFullyQualifiedName(), GlobalSearchScope.allScope(project));

            if (eventHandlerAnnotation != null) {
                new ImportHelper(CodeStyleSettingsManager.getSettings(project)).addImport((PsiJavaFile) file, eventHandlerAnnotation);
            }
        }
    }

    private String getRequiredProperty(PsiClass classType) {
        if (eventHandler.getRequiredProperty() != null) {
            if (classType != null) {
                PsiField[] allFields = classType.getFields();
                if (allFields.length > 0 && allFields[0] != null) {
                    PsiField firstFieldOfAnnotationArgument = allFields[0];
                    return "(" + eventHandler.getRequiredProperty() + "=  \"" + firstFieldOfAnnotationArgument.getNameIdentifier().getText() + "\")";
                } else {
                    return "(" + eventHandler.getRequiredProperty() + "=  \"\")";
                }
            }
        }
        return "";
    }
}
