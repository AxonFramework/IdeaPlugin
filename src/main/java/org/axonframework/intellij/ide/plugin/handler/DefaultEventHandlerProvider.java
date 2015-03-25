package org.axonframework.intellij.ide.plugin.handler;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.Query;

class DefaultEventHandlerProvider implements EventHandlerProvider {
    @Override
    public void scanHandlers(Project project, GlobalSearchScope scope, final Registrar registrar) {
        for (final AnnotationTypes annotationType : AnnotationTypes.values()) {
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
                                EventHandler handler = createHandlerBasedOnAnnotation(method, annotation);
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
    public EventHandler resolve(PsiElement element) {
        if (element instanceof PsiMethod) {
            PsiMethod methodElement = (PsiMethod) element;
            final PsiAnnotation annotation = locateAnnotation(methodElement);
            if (annotation != null) {
                return createHandlerBasedOnAnnotation(methodElement, annotation);
            }
        }
        return null;
    }

    private EventHandler createHandlerBasedOnAnnotation(PsiMethod method, PsiAnnotation annotation) {
        if (annotation.getText().equals("@CommandHandler")) {
            return CommandEventHandler.createEventHandler(method);
        } else {
            return DefaultEventHandler.createEventHandler(method, annotation);
        }
    }

    private PsiAnnotation locateAnnotation(PsiMethod element) {
        for (AnnotationTypes annotationType : AnnotationTypes.values()) {
            PsiAnnotation annotation = element.getModifierList().findAnnotation(annotationType.getFullyQualifiedName());
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }
}
