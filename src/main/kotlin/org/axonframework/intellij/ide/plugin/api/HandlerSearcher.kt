package org.axonframework.intellij.ide.plugin.api

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import org.axonframework.intellij.ide.plugin.resolving.AnnotationResolver
import org.axonframework.intellij.ide.plugin.util.axonScope

/**
 * HandlerSearchers are responsible for finding the handlers of their `MessageHandlerType`.
 * They can choose to add additional information in their `MessageHandlerType` so this can be used when the handler
 * is displayed.
 *
 * In order to add a new HandlerSearcher:
 * - Add the Type to `MessageHandlerType`
 * - Create a `Handler` implementation, responsible for containing all data related to the handler.
 * - Create a `HandlerSearcher` implementation, responsible for creating the `Handler` instance.
 * - Add your handler to `plugin.xml` as an extension to the `org.axonframework.intellij.axonplugin.handlerSearcher` extension point.
 *
 * @see HandlerSearcher
 * @see Handler
 */
abstract class HandlerSearcher(private val handlerType: MessageHandlerType) {
    abstract fun createMessageHandler(method: PsiMethod): Handler?

    open fun search(project: Project): List<Handler> {
        val annotationClasses = findAllRelevantAnnotationClasses(project)
        val annotatedMethods = annotationClasses.flatMap {
            AnnotatedElementsSearch.searchPsiMethods(it, project.axonScope()).findAll()
        }
        return annotatedMethods.mapNotNull { this.createMessageHandler(it) }.distinct()
    }

    private fun findAllRelevantAnnotationClasses(project: Project): List<PsiClass> {
        return project.getService(AnnotationResolver::class.java).getAnnotationClassesForType(handlerType)
    }
}
