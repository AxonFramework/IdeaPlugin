package org.axonframework.intellij.ide.plugin.publisher;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.Query;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

class DefaultEventPublisherProvider implements EventPublisherProvider {

    private final Map<Project, List<PsiMethod>> publisherMethodsPerProject = new ConcurrentHashMap<Project, List<PsiMethod>>();

    @Override
    public void scanPublishers(Project project, GlobalSearchScope scope, final Registrar registrar) {
        cleanClosedProjects();
        final CopyOnWriteArrayList<PsiMethod> methods = new CopyOnWriteArrayList<PsiMethod>();
        publisherMethodsPerProject.put(project, methods);
        methods.addAll(findMethods(project, GlobalSearchScope.allScope(project),
                                   "org.axonframework.eventsourcing.AbstractEventSourcedAggregateRoot", "apply"));
        methods.addAll(findMethods(project, GlobalSearchScope.allScope(project),
                                   "org.axonframework.domain.AbstractAggregateRoot", "registerEvent"));
        methods.addAll(findMethods(project, GlobalSearchScope.allScope(project),
                                   "org.axonframework.eventsourcing.AbstractEventSourcedEntity", "apply"));

        for (PsiMethod method : methods) {
            Query<PsiReference> invocations = ReferencesSearch.search(method, scope);
            invocations.forEachAsync(new Processor<PsiReference>() {
                @Override
                public boolean process(PsiReference psiReference) {
                    PsiMethodCallExpression methodCall = (PsiMethodCallExpression) PsiTreeUtil.findFirstParent(
                            psiReference.getElement(),
                            new Condition<PsiElement>() {
                                @Override
                                public boolean value(PsiElement psiElement) {
                                    return psiElement instanceof PsiMethodCallExpression;
                                }
                            });
                    if (methodCall != null) {
                        PsiType[] expressionTypes = methodCall.getArgumentList().getExpressionTypes();
                        if (methodCall.getMethodExpression().getReference() != null) {
                            final PsiMethod referencedMethod = (PsiMethod) methodCall.getMethodExpression()
                                                                                     .getReference().resolve();
                            if (referencedMethod != null) {
                                if (expressionTypes.length > 0) {
                                    registrar.registerPublisher(new DefaultEventPublisher(expressionTypes[0], methodCall));
                                }
                            }
                        }
                    }
                    return true;
                }
            });
        }
    }

    private void cleanClosedProjects() {
        for (Project project : publisherMethodsPerProject.keySet()) {
            if (!project.isOpen()) {
                publisherMethodsPerProject.remove(project);
            }
        }
    }

    private List<PsiMethod> findMethods(Project project, GlobalSearchScope allScope, String className,
                                        String methodName) {
        PsiClass aggregateClass = JavaPsiFacade.getInstance(project).findClass(
                className,
                allScope);
        if (aggregateClass != null) {
            return Arrays.asList(aggregateClass.findMethodsByName(methodName, true));
        }
        return Collections.emptyList();
    }

    @Override
    public EventPublisher resolve(PsiElement element) {
        List<PsiMethod> methods = publisherMethodsPerProject.get(element.getProject());
        if (element instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression expression = (PsiMethodCallExpression) element;
            PsiType[] expressionTypes = expression.getArgumentList().getExpressionTypes();
            for (PsiMethod method : methods) {
                if (expression.getMethodExpression().getReference() != null
                        && expression.getMethodExpression().isReferenceTo(method)) {
                    return new DefaultEventPublisher(expressionTypes[0], element);
                }
            }
        }
        return null;
    }
}
