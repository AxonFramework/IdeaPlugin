package org.axonframework.intellij.ide.plugin.commandhandler;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import org.axonframework.intellij.ide.plugin.eventhandler.IsMethodCondition;

class DefaultCommandHandlerProvider implements CommandHandlerProvider {

    @Override
    public void scanHandlers(Project project, GlobalSearchScope scope, final Registrar registrar) {
        for (CommandAnnotationTypes annotationType : CommandAnnotationTypes.values()) {
            PsiClass eventHandlerAnnotation = JavaPsiFacade.getInstance(project)
                                                           .findClass(annotationType.getFullyQualifiedName(),
                                                                      GlobalSearchScope.allScope(project));
            if (eventHandlerAnnotation != null) {
                Query<PsiReference> annotationUsages = ReferencesSearch.search(eventHandlerAnnotation, scope);
                annotationUsages.forEachAsync(new Processor<PsiReference>() {
                    @Override
                    public boolean process(PsiReference psiReference) {
                        PsiMethod method = (PsiMethod) PsiTreeUtil.findFirstParent(psiReference.getElement(),
                                                                                   new IsMethodCondition());
                        if (method != null) {
                            // this doesn't say the method is annotated
                            final PsiAnnotation annotation = locateAnnotation(method);
                            if (annotation != null) {
                                CommandHandler handler = DefaultCommandHandler.createEventHandler(method, annotation);
                                if (handler != null) {
                                    registrar.registerHandler(handler);
                                }
                            }
                        }
                        return true;
                    }
                });
            }
        }
    }

    @Override
    public CommandHandler resolve(PsiElement element) {
        if (element instanceof PsiMethod) {
            final PsiAnnotation annotation = locateAnnotation((PsiMethod) element);
            if (annotation != null) {
                return DefaultCommandHandler.createEventHandler((PsiMethod) element, annotation);
            }
        }
        return null;
    }

    private PsiAnnotation locateAnnotation(PsiMethod element) {
        for (CommandAnnotationTypes annotationType : CommandAnnotationTypes.values()) {
            PsiAnnotation annotation = element.getModifierList().findAnnotation(annotationType.getFullyQualifiedName());
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }
}
