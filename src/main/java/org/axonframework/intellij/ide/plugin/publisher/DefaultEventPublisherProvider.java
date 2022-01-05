package org.axonframework.intellij.ide.plugin.publisher;

import static java.util.Arrays.asList;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.axonframework.intellij.ide.plugin.handler.AnnotationTypes;


import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiNewExpression;
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

class DefaultEventPublisherProvider implements PublisherProvider {

    private final ConcurrentHashMap<Project, Set<PsiElement>> publisherMethodsPerProject = new ConcurrentHashMap<Project, Set<PsiElement>>();

    @Override
    public void scanPublishers(final Project project, GlobalSearchScope scope, final Registrar registrar) {
        cleanClosedProjects();
        publisherMethodsPerProject.putIfAbsent(project, new HashSet<>());
        Set<PsiElement> psiMethods = publisherMethodsPerProject.get(project);
        psiMethods.addAll(findMethods(project, GlobalSearchScope.allScope(project),
                "org.axonframework.modelling.command.AggregateLifecycle", "apply"));
        psiMethods.addAll(findMethods(project, GlobalSearchScope.allScope(project),
                "org.axonframework.modelling.command.AggregateLifecycle", "apply"));

        GlobalSearchScope scopeNarrowedToJavaSourceFiles =
                GlobalSearchScope.getScopeRestrictedByFileTypes(scope, StdFileTypes.JAVA);
        scanEventPublishers(project, scopeNarrowedToJavaSourceFiles, registrar);
        scanCommandPublishers(project, scopeNarrowedToJavaSourceFiles, registrar);
    }

    private void scanCommandPublishers(final Project project, GlobalSearchScope scope, final Registrar registrar) {
        PsiClass commandHandlerAnnotation = findCommandHandlersAnnotation(project);
        if (commandHandlerAnnotation != null) {
            Query<PsiReference> annotationUsages =
                    ReferencesSearch.search(commandHandlerAnnotation, scope);
            annotationUsages.forEachAsync(new Processor<PsiReference>() {
                @Override
                public boolean process(PsiReference psiReference) {
                    PsiMethod method = (PsiMethod) PsiTreeUtil.findFirstParent(psiReference.getElement(),
                            new IsMethodWithParameterCondition());
                    if (methodHasParameter(method)) {
                        PsiAnnotation methodAnnotation = method.getModifierList().findAnnotation(AnnotationTypes.COMMAND_EVENT_HANDLER.getFullyQualifiedName());
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
                    GlobalSearchScope scopeNarrowedToJavaSourceFiles = GlobalSearchScope.allScope(project);
                    PsiClass parameterClass = JavaPsiFacade.getInstance(project)
                            .findClass(type.getCanonicalText(), scopeNarrowedToJavaSourceFiles);
                    if (parameterClass != null) {
                        PsiMethod[] constructors = parameterClass.getConstructors();
                        if (parameterClassHasConstructor(constructors)) {
                            registerAllConstructorInvocations(type, constructors);
                        }
                    }
                }

                private void registerAllConstructorInvocations(final PsiType type, PsiMethod[] constructors) {
                    for (PsiMethod constructor : constructors) {
                        Query<PsiReference> constructorCalls = MethodReferencesSearch.search(constructor);
                        constructorCalls.forEachAsync(new Processor<PsiReference>() {
                            @Override
                            public boolean process(PsiReference psiReference) {
                                CommandEventPublisher eventPublisher = new CommandEventPublisher(type, psiReference.getElement());
                                registrar.registerPublisher(eventPublisher);
                                Set<PsiElement> psiMethods = publisherMethodsPerProject.get(project);
                                psiMethods.add(psiReference.getElement());

                                return true;
                            }
                        });
                    }
                }
            });
        }
    }

    private boolean parameterClassHasConstructor(PsiMethod[] constructors) {
        return constructors.length > 0;
    }

    private boolean methodHasParameter(PsiMethod method) {
        return method != null && method.getParameterList().getParametersCount() > 0;
    }

    private PsiClass findCommandHandlersAnnotation(Project project) {
        return JavaPsiFacade.getInstance(project)
                .findClass("org.axonframework.commandhandling.CommandHandler",
                        GlobalSearchScope.allScope(project));
    }

    private void scanEventPublishers(Project project, GlobalSearchScope scope, final Registrar registrar) {
        for (final PsiElement method : publisherMethodsPerProject.get(project)) {
            Query<PsiReference> invocations =
                    MethodReferencesSearch.search((PsiMethod) method, scope, false);
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
        for (Project project : Collections.list(publisherMethodsPerProject.keys())) {
            if (!project.isOpen()) {
                publisherMethodsPerProject.remove(project);
            }
        }
    }

    private Set<PsiMethod> findMethods(Project project, GlobalSearchScope allScope, String className,
                                       String methodName) {
        PsiClass aggregateClass = JavaPsiFacade.getInstance(project).findClass(className, allScope);
        if (aggregateClass != null) {
            return new HashSet<PsiMethod>(asList(aggregateClass.findMethodsByName(methodName, true)));
        }
        return Collections.emptySet();
    }

    @Override
    public Publisher resolve(PsiElement element) {
        if (element instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression expression = (PsiMethodCallExpression) element;
            for (PsiElement method : publisherMethodsPerProject.get(element.getProject())) {
                boolean hasReference = expression.getMethodExpression().getReference() != null;
                if (hasReference
                        && expression.getMethodExpression().isReferenceTo(method)) {
                    PsiType[] expressionTypes = expression.getArgumentList().getExpressionTypes();
                    return new DefaultEventPublisher(expressionTypes[0], element);
                }
            }
        }
        if (element instanceof PsiNewExpression) {
            PsiNewExpression expression = (PsiNewExpression) element;

            for (PsiElement classType : publisherMethodsPerProject.get(element.getProject())) {
                if (expression.getClassReference() != null && classType.isEquivalentTo(expression.getClassReference())) {
                    return new CommandEventPublisher(expression.getType(), element);
                }
            }
        }
        return null;
    }
}
