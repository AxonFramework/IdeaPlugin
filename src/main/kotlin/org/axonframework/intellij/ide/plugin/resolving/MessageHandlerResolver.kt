package org.axonframework.intellij.ide.plugin.resolving

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.api.HANDLER_SEARCHER_EP
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.util.areAssignable
import org.axonframework.intellij.ide.plugin.util.createCachedValue

/**
 * Searches the codebase for Message handlers based on the annotations defined in MessageHandlerType.
 *
 * Results are cached based on the Psi modifications of IntelliJ. This means the calculations are invalidated when
 * the PSI is modified (code is edited) or is collected by the garbage collector.
 *
 * @see org.axonframework.intellij.ide.plugin.api.MessageHandlerType
 */
class MessageHandlerResolver(private val project: Project) {
    private val handlerCache = project.createCachedValue { executeFindMessageHandlers() }

    fun findHandlersForType(qualifiedName: String): List<Handler> {
        return handlerCache.value
                .filter { areAssignable(project, it.payloadFullyQualifiedName, qualifiedName) }
                .filter { it.element.isValid }
    }

    fun findAllHandlers(): List<Handler> = handlerCache.value

    fun findHandlerByElement(psiElement: PsiElement): Handler? {
        return handlerCache.value.firstOrNull { it.element == psiElement }
    }

    private fun executeFindMessageHandlers(): List<Handler> {
        return HANDLER_SEARCHER_EP.extensionList
                .flatMap { it.search(project) }
                .distinct()
    }
}

