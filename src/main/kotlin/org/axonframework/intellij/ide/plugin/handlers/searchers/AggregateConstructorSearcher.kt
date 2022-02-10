package org.axonframework.intellij.ide.plugin.handlers.searchers

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.handlers.types.AggregateConstructor
import org.axonframework.intellij.ide.plugin.resolving.ResolvedAnnotation
import org.axonframework.intellij.ide.plugin.util.annotationResolver
import org.axonframework.intellij.ide.plugin.util.axonScope

/**
 * Searches for constructors of classes annotated with Aggregate and a constructor without CommandHandler annotation.
 * This is applicable in the case of invariants.
 *
 * @see AggregateConstructor
 */
class AggregateConstructorSearcher : AbstractHandlerSearcher(MessageHandlerType.COMMAND) {

    /**
     * The default search of AbstractSearchHandler is overridden heren since we don't want to search based on
     * annotations, but based on all constructor references of all aggregates.
     */
    override fun search(project: Project): List<Handler> {
        return project.annotationResolver()
                .getAnnotationClasses(AxonAnnotation.AGGREGATE_ROOT)
                .flatMap { annotation ->
                    searchAggregateConstructorsForAnnotation(annotation, project)
                }
    }

    private fun searchAggregateConstructorsForAnnotation(annotation: ResolvedAnnotation, project: Project): List<Handler> {
        return AnnotatedElementsSearch.searchPsiClasses(annotation.psiClass, project.axonScope()).findAll()
                .flatMap { it.constructors.toList() }
                .filter { !it.hasAnnotation(MessageHandlerType.COMMAND.annotationName) && it.hasParameters() }
                .mapNotNull { createMessageHandler(it) }
    }

    override fun createMessageHandler(method: PsiMethod): Handler {
        return AggregateConstructor(method, method.containingClass?.qualifiedName!!)
    }
}
