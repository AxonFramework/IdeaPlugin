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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Allard Buijze
 */
public class DefaultEventPublisherProvider implements EventPublisherProvider {

    private List<PsiMethod> methods = new ArrayList<PsiMethod>();

    @Override
    public void scanPublishers(Project project, GlobalSearchScope scope, final Registrar registrar) {
        methods.addAll(findMethods(project, GlobalSearchScope.allScope(project),
                                   "org.axonframework.eventsourcing.AbstractEventSourcedAggregateRoot", "apply"));
        methods.addAll(findMethods(project, GlobalSearchScope.allScope(project),
                                   "org.axonframework.domain.AbstractAggregateRoot", "registerEvent"));

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
                                    registrar.registerPublisher(new EventPublisherImpl(expressionTypes[0], methodCall));
                                }
                            }
                        }
                    }
                    return true;
                }
            });
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
        if (element instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression expression = (PsiMethodCallExpression) element;
            PsiType[] expressionTypes = expression.getArgumentList().getExpressionTypes();
            for (PsiMethod method : methods) {
                if (expression.getMethodExpression().getReference() != null
                        && expression.getMethodExpression().isReferenceTo(method)) {
                    return new EventPublisherImpl(expressionTypes[0], element);
                }
            }
        }
        return null;
    }
}
