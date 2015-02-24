package org.axonframework.intellij.ide.plugin.publisher;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import com.intellij.util.containers.ConcurrentMultiMap;
import com.intellij.util.containers.MultiMap;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

class DefaultEventPublisherProvider implements EventPublisherProvider {

    private final MultiMap<Project, PsiMethod> publisherMethodsPerProject = new ConcurrentMultiMap<Project, PsiMethod>();

    @Override
    public void scanPublishers(final Project project, GlobalSearchScope scope, final Registrar registrar) {
        cleanClosedProjects();
        publisherMethodsPerProject.putValues(project, findMethods(project, GlobalSearchScope.allScope(project),
                "org.axonframework.eventsourcing.AbstractEventSourcedAggregateRoot", "apply"));
        publisherMethodsPerProject.putValues(project, findMethods(project, GlobalSearchScope.allScope(project),
                "org.axonframework.domain.AbstractAggregateRoot", "registerEvent"));
        publisherMethodsPerProject.putValues(project, findMethods(project, GlobalSearchScope.allScope(project),
                "org.axonframework.eventsourcing.AbstractEventSourcedEntity", "apply"));

        scanEventPublishers(scope, registrar);
        scanCommandPublishers(project, scope, registrar);
    }

    private void scanCommandPublishers(final Project project, GlobalSearchScope scope, final Registrar registrar) {
        PsiClass commandHandlerAnnotation = findCommandHandlersAnnotation(project);
        if (commandHandlerAnnotation != null) {
            Query<PsiReference> annotationUsages = ReferencesSearch.search(commandHandlerAnnotation, scope);
            annotationUsages.forEachAsync(new Processor<PsiReference>() {
                @Override
                public boolean process(PsiReference psiReference) {
                    PsiMethod method = (PsiMethod) PsiTreeUtil.findFirstParent(psiReference.getElement(),
                            new IsMethodWithParameterCondition());
                    if (methodHasParameter(method)) {
                        PsiAnnotation methodAnnotation = method.getModifierList().findAnnotation("org.axonframework.commandhandling.annotation.CommandHandler");
                        PsiParameter firstCommandHandlerArgument = method.getParameterList().getParameters()[0];
                        if (methodIsAnnotatedAsCommandHandler(methodAnnotation)) {
                            PsiTypeElement firstCommandHandlerArgumentType = firstCommandHandlerArgument.getTypeElement();
                            if (methodArgumentIsTyped(firstCommandHandlerArgumentType)) {
                                findAndRegisterAllConstructors(firstCommandHandlerArgumentType);
                            }
                        }
                    }
                    return true;
                }

                private boolean methodArgumentIsTyped(PsiTypeElement firstCommandHandlerArgumentType) {
                    return firstCommandHandlerArgumentType != null;
                }

                private boolean methodIsAnnotatedAsCommandHandler(PsiAnnotation methodCommandHandlerAnnotation) {
                    return methodCommandHandlerAnnotation != null;
                }

                private void findAndRegisterAllConstructors(PsiTypeElement firstCommandHandlerArgumentType) {
                    final PsiType type = firstCommandHandlerArgumentType.getType();
                    PsiClass parameterClass = JavaPsiFacade.getInstance(project)
                            .findClass(type.getCanonicalText(), GlobalSearchScope.allScope(project));
                    if (parameterClass != null) {
                        PsiMethod[] constructors = parameterClass.getConstructors();
                        if (parameterClassHasConstructor(constructors)) {
                            registerAllConstructorInvocations(type, constructors);
                        }
                    }
                }

                private boolean parameterClassHasConstructor(PsiMethod[] constructors) {
                    return constructors.length > 0;
                }

                private void registerAllConstructorInvocations(final PsiType type, PsiMethod[] constructors) {
                    for (PsiMethod constructor : constructors) {
                        Query<PsiReference> constructorCalls = MethodReferencesSearch.search(constructor);
                        constructorCalls.forEachAsync(new Processor<PsiReference>() {
                            @Override
                            public boolean process(PsiReference psiReference) {
                                registrar.registerPublisher(new CommandEventPublisher(type, psiReference.getElement()));
                                return true;
                            }
                        });
                    }
                }

                private boolean methodHasParameter(PsiMethod method) {
                    return method != null && method.getParameterList().getParametersCount() > 0;
                }
            });
        }
    }

    private PsiClass findCommandHandlersAnnotation(Project project) {
        return JavaPsiFacade.getInstance(project)
                .findClass("org.axonframework.commandhandling.annotation.CommandHandler",
                        GlobalSearchScope.allScope(project));
    }

    private void scanEventPublishers(GlobalSearchScope scope, final Registrar registrar) {
        for (PsiMethod method : publisherMethodsPerProject.values()) {
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
                            if (referencedMethod != null && expressionTypes.length > 0) {
                                registrar.registerPublisher(new DefaultEventPublisher(expressionTypes[0], methodCall));
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
        PsiClass aggregateClass = JavaPsiFacade.getInstance(project).findClass(className, allScope);
        if (aggregateClass != null) {
            return asList(aggregateClass.findMethodsByName(methodName, true));
        }
        return Collections.emptyList();
    }

    @Override
    public EventPublisher resolve(PsiElement element) {
        if (element instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression expression = (PsiMethodCallExpression) element;
            PsiType[] expressionTypes = expression.getArgumentList().getExpressionTypes();
            for (PsiMethod method : publisherMethodsPerProject.get(element.getProject())) {
                if (expression.getMethodExpression().getReference() != null
                        && expression.getMethodExpression().isReferenceTo(method)) {
                    return new DefaultEventPublisher(expressionTypes[0], element);
                }
            }
        }
        return null;
    }
}
