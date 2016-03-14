package org.axonframework.intellij.ide.plugin.handler;

import static com.intellij.psi.search.GlobalSearchScope.projectScope;
import static com.intellij.psi.search.searches.AnnotatedElementsSearch.searchPsiClasses;

import java.util.Map;


import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import com.intellij.util.containers.HashMap;

class DefaultEventHandlerProvider implements HandlerProvider {
    @Override
    public void scanHandlers(final Project project, final GlobalSearchScope scope, final Registrar registrar) {
        for (final AnnotationTypes annotationType : AnnotationTypes.values()) {
            final PsiClass eventHandlerAnnotation = JavaPsiFacade.getInstance(project)
                    .findClass(annotationType.getFullyQualifiedName(),
                            GlobalSearchScope.allScope(project));

            if (eventHandlerAnnotation != null) {
                final Query<PsiClass> customEventHandlers = searchPsiClasses(eventHandlerAnnotation, scope);
                customEventHandlers.forEach(new Processor<PsiClass>() {
                    @Override
                    public boolean process(final PsiClass psiClass) {
                        registerEventHandlerAnnotation(scope, registrar, eventHandlerAnnotation, project);
                        return true;
                    }
                });
            }

            registerEventHandlerAnnotation(scope, registrar, eventHandlerAnnotation, project);
        }
    }

    private void registerEventHandlerAnnotation(final GlobalSearchScope scope, final Registrar registrar,
                                                final PsiClass eventHandlerAnnotation, final Project project) {
        if (eventHandlerAnnotation != null) {
            Query<PsiReference> annotationUsages = ReferencesSearch.search(eventHandlerAnnotation, scope);
            annotationUsages.forEachAsync(new Processor<PsiReference>() {
                @Override
                public boolean process(PsiReference psiReference) {
                    PsiMethod method = (PsiMethod) PsiTreeUtil.findFirstParent(psiReference.getElement(),
                            new IsMethodCondition());
                    if (method != null) {
                        // this doesn't say the method is annotated
                        final PsiAnnotation annotation = locateAnnotation(method, project);
                        if (annotation != null) {
                            Handler handler = createHandlerBasedOnAnnotation(method, annotation);
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

    @Override
    public Handler resolve(PsiElement element, final Project project) {
        if (element instanceof PsiMethod) {
            PsiMethod methodElement = (PsiMethod) element;
            final PsiAnnotation annotation = locateAnnotation(methodElement, project);
            if (annotation != null) {
                return createHandlerBasedOnAnnotation(methodElement, annotation);
            }
        }
        return null;
    }

    private Handler createHandlerBasedOnAnnotation(PsiMethod method, PsiAnnotation annotation) {
        if (AnnotationTypes.COMMAND_EVENT_HANDLER.getFullyQualifiedName().equals(annotation.getQualifiedName())) {
            return CommandEventHandler.createEventHandler(method);
        } else {
            return DefaultEventHandler.createEventHandler(method, annotation);
        }
    }

    private PsiAnnotation locateAnnotation(PsiMethod element, final Project project) {
        final Map<String, String> fullyQualifiedNames = getFullyQualifiedNames(project);
        for (String fullyQualifiedName : fullyQualifiedNames.keySet()) {
            PsiAnnotation annotation = element.getModifierList().findAnnotation(fullyQualifiedName);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    private Map<String, String> getFullyQualifiedNames(final Project project) {
        final HashMap<String, String> annotations = new HashMap<String, String>();

        for (AnnotationTypes annotationType : AnnotationTypes.values()) {
            annotations.put(annotationType.getFullyQualifiedName(), annotationType.getAnnotation());

            final PsiClass eventHandlerAnnotation = JavaPsiFacade.getInstance(project)
                    .findClass(annotationType.getFullyQualifiedName(),
                            GlobalSearchScope.allScope(project));

            if (eventHandlerAnnotation != null) {
                final Query<PsiClass> customEventHandlers = searchPsiClasses(eventHandlerAnnotation, projectScope(project));
                customEventHandlers.forEach(new Processor<PsiClass>() {
                    @Override
                    public boolean process(final PsiClass psiClass) {
                        annotations.put(psiClass.getQualifiedName(), psiClass.getName());
                        return true;
                    }
                });
            }
        }
        return annotations;
    }
}
