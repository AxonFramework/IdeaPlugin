package org.axonframework.intellij.ide.plugin.resolving

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.search.searches.MethodReferencesSearch
import com.intellij.psi.util.CachedValue
import org.axonframework.intellij.ide.plugin.api.MessageCreator
import org.axonframework.intellij.ide.plugin.creators.DefaultMessageCreator
import org.axonframework.intellij.ide.plugin.util.areAssignable
import org.axonframework.intellij.ide.plugin.util.axonScope
import org.axonframework.intellij.ide.plugin.util.createCachedValue
import org.axonframework.intellij.ide.plugin.util.sortingByDisplayName
import java.util.concurrent.ConcurrentHashMap

/**
 * Searches the codebase for places where a message payload is constructed.
 * It does this by searching for constructor references of compatible payloads. Inheritance is supported.
 *
 * Results are cached based on the Psi modifications of IntelliJ. This means the calculations are invalidated when
 * the PSI is modified (code is edited) or is collected by the garbage collector.
 *
 * Note: We could scan all constructors, or all constructors of handler types. However, this is very much
 * disliked by IntelliJ performance-wise, so we search only for the file currently open (that is showing line markers).
 */
class MessageCreationResolver(private val project: Project) {
    private val constructorPublisherCache = ConcurrentHashMap<String, CachedValue<List<MessageCreator>>>()

    fun getCreatorsForPayload(payloadQualifiedClassName: String): List<MessageCreator> {
        val cache = constructorPublisherCache.getOrPut(payloadQualifiedClassName) {
            project.createCachedValue { resolveCreatorsForPayload(payloadQualifiedClassName) }
        }
        return cache.value
    }

    private fun resolveCreatorsForPayload(qualifiedName: String): List<MessageCreator> {
        val handlerResolver = project.getService(MessageHandlerResolver::class.java)

        // We need to take care of inheritance, so we find all classes assignable to the referenced type.
        // Limitation: We only consider types which have a handler
        val psiFacade = JavaPsiFacade.getInstance(project)
        val classesForQualifiedName = handlerResolver.findAllHandlers()
                .map { it.payloadFullyQualifiedName }.distinct()
                .filter { areAssignable(project, qualifiedName, it) }
                .mapNotNull { psiFacade.findClass(it, project.axonScope()) }

        val constructors = classesForQualifiedName.flatMap { it.constructors.toList() }
        val constructorInvocations = constructors
                .flatMap { MethodReferencesSearch.search(it, project.axonScope(), true) }
                .map { ref -> createCreator(ref.element, qualifiedName) }
                .distinct()

        return constructorInvocations.sortedWith(project.sortingByDisplayName())
    }

    private fun createCreator(element: PsiElement, qualifiedName: String): MessageCreator {
        return DefaultMessageCreator(element, qualifiedName)
    }
}
