package org.axonframework.intellij.ide.plugin.handler;

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
import org.axonframework.intellij.ide.plugin.IsMethodCondition;

/**
 * @author Allard Buijze
 */
public class DefaultEventHandlerProvider implements EventHandlerProvider {

    private static final String[] annotationTypes = new String[]{
            "org.axonframework.eventhandling.annotation.EventHandler",
            "org.axonframework.eventsourcing.annotation.EventSourcingHandler",
            "org.axonframework.saga.annotation.SagaEventHandler"};

    @Override
    public void scanHandlers(Project project, GlobalSearchScope scope, final Registrar registrar) {
        for (String annotationType : annotationTypes) {
            PsiClass eventHandlerAnnotation = JavaPsiFacade.getInstance(project)
                                                           .findClass(annotationType,
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
                                EventHandler handler = EventHandlerImpl.createEventHandler(method, annotation);
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
            final PsiAnnotation annotation = locateAnnotation((PsiMethod) element);
            if (annotation != null) {
                return EventHandlerImpl.createEventHandler((PsiMethod) element, annotation);
            }
        }
        return null;
    }

    private PsiAnnotation locateAnnotation(PsiMethod element) {
        for (String annotationType : annotationTypes) {
            PsiAnnotation annotation = element.getModifierList().findAnnotation(annotationType);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }
}
