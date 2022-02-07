package org.axonframework.intellij.ide.plugin.handlers.searchers

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import org.axonframework.intellij.ide.plugin.api.AxonAnnotation
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.handlers.types.AggregateConstructor
import org.axonframework.intellij.ide.plugin.util.allScope
import org.axonframework.intellij.ide.plugin.util.axonScope

/**
 * Searches for constructors of classes annotated with Aggregate and a constructor without CommandHandler annotation.
 * This is applicable in the case of invariants. We use the aggregate type as payload here, so it can be linked together later.
 */
class AggregateConstructorSearcher : AbstractHandlerSearcher(MessageHandlerType.COMMAND) {
    override fun search(project: Project): List<Handler> {
        val annotation = JavaPsiFacade.getInstance(project).findClass(AxonAnnotation.AGGREGATE.annotationName, project.allScope())
                ?: return emptyList()
        val aggregates = AnnotatedElementsSearch.searchPsiClasses(annotation, project.axonScope()).findAll()
        return aggregates
                .flatMap { it.constructors.toList() }
                .filter {
                    !it.hasAnnotation(MessageHandlerType.COMMAND.annotationName) && it.hasParameters()
                }.mapNotNull {
                    createMessageHandler(it)
                }

    }

    override fun createMessageHandler(method: PsiMethod): Handler {
        return AggregateConstructor(method, method.containingClass?.qualifiedName!!)
    }
}
