package org.axonframework.intellij.ide.plugin.resolving

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.search.searches.MethodReferencesSearch
import com.intellij.psi.util.CachedValue
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageCreator
import org.axonframework.intellij.ide.plugin.creators.DefaultMessageCreator
import org.axonframework.intellij.ide.plugin.util.areAssignable
import org.axonframework.intellij.ide.plugin.util.axonScope
import org.axonframework.intellij.ide.plugin.util.createCachedValue
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.toUElement
import java.util.concurrent.ConcurrentHashMap

/**
 * Searches the codebase for places where a message payload is constructed.
 * It does this by searching for constructor references of compatible payloads. Inheritance is supported.
 *
 * Results are cached based on the Psi modifications of IntelliJ. This means the calculations are invalidated when
 * the PSI is modified (code is edited) or is collected by the garbage collector.
 */
class MessageCreationResolver(private val project: Project) {
    private val psiFacade = JavaPsiFacade.getInstance(project)
    private val handlerResolver = project.getService(MessageHandlerResolver::class.java)
    private val constructorsByPayloadCache = ConcurrentHashMap<String, CachedValue<List<MessageCreator>>>()

    fun getCreatorsForPayload(payloadQualifiedClassName: String): List<MessageCreator> {
        val cache = constructorsByPayloadCache.getOrPut(payloadQualifiedClassName) {
            project.createCachedValue {
                resolveCreatorForFqn(payloadQualifiedClassName)
            }
        }
        return cache.value
    }

    /**
     * Finds the already constructed/found creator in the caches. Useful for quick filtering in line marker popups.
     */
    fun findCreatorByElement(element: PsiElement): MessageCreator? {
        return constructorsByPayloadCache.values.filter { it.hasUpToDateValue() }
                .flatMap { it.value }
                .firstOrNull { it.element == element }
    }

    private fun resolveCreatorForFqn(qualifiedName: String): List<MessageCreator> {
        val classesForQualifiedName = handlerResolver.findAllHandlers()
                .map { it.payloadFullyQualifiedName }.distinct()
                .filter { areAssignable(project, qualifiedName, it) }
        return resolveCreatorsForFqns(classesForQualifiedName)
    }

    /**
     * This action is VERY expensive. Should only be used if the user does not depend on it or is expected to wait.
     * For example, when creating an Event Modeling board based on this info.
     */
    fun resolveAllCreators(): List<MessageCreator> {
        val handlers = handlerResolver.findAllHandlers()
        val handlerTypes = handlers.map { it.payloadFullyQualifiedName }.distinct()

        return resolveCreatorsForFqns(handlerTypes)
    }

    private fun resolveCreatorsForFqns(fqns: List<String>): List<MessageCreator> {
        return fqns.flatMap { typeFqn ->
            val clazz = psiFacade.findClass(typeFqn, project.axonScope()) ?: return@flatMap emptyList()
            clazz.constructors
                    .flatMap { MethodReferencesSearch.search(it, project.axonScope(), true) }
                    .flatMap { ref -> createCreators(ref.element, typeFqn) }
                    .distinct()
        }
    }

    private fun createCreators(element: PsiElement, qualifiedName: String): List<MessageCreator> {
        val parentHandlers = findParentHandlers(element)
        if (parentHandlers.isEmpty()) {
            return listOf(DefaultMessageCreator(element, qualifiedName, null))
        }
        return parentHandlers.map { DefaultMessageCreator(element, qualifiedName, it) }
    }

    /**
     * Finds all parent handlers of a method. This is kind of intense on IntelliJ, so we should monitor performance
     * on this and perhaps reduce the recursion limit. The recursion limit the depth of the call tree that is searched.
     */
    private fun findParentHandlers(element: PsiElement, depth: Int = 0): List<Handler> {
        if (depth > 3) {
            // Recursion guard
            return listOf()
        }
        val parent = element.toUElement()?.getParentOfType<UMethod>()?.javaPsi ?: return listOf()
        val parentHandler = handlerResolver.findHandlerByElement(parent)
        if (parentHandler != null) {
            return listOf(parentHandler)
        }

        val references = MethodReferencesSearch.search(parent, element.project.axonScope(), true)
        return references.flatMap { findParentHandlers(it.element, depth + 1) }
    }
}
