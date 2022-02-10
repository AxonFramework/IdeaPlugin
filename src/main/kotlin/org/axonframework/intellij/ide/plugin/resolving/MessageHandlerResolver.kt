package org.axonframework.intellij.ide.plugin.resolving

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageType
import org.axonframework.intellij.ide.plugin.handlers.searchers.AggregateConstructorSearcher
import org.axonframework.intellij.ide.plugin.handlers.searchers.CommandHandlerInterceptorSearcher
import org.axonframework.intellij.ide.plugin.handlers.searchers.CommandHandlerSearcher
import org.axonframework.intellij.ide.plugin.handlers.searchers.EventHandlerSearcher
import org.axonframework.intellij.ide.plugin.handlers.searchers.EventSourcingHandlerSearcher
import org.axonframework.intellij.ide.plugin.handlers.searchers.QueryHandlerSearcher
import org.axonframework.intellij.ide.plugin.handlers.searchers.SagaEventHandlerSearcher
import org.axonframework.intellij.ide.plugin.util.PerformanceRegistry
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
    private val searchers = listOf(
            CommandHandlerInterceptorSearcher(),
            CommandHandlerSearcher(),
            EventHandlerSearcher(),
            EventSourcingHandlerSearcher(),
            QueryHandlerSearcher(),
            SagaEventHandlerSearcher(),
            AggregateConstructorSearcher()
    )

    private val handlerCache = project.createCachedValue {
        PerformanceRegistry.measure("MessageHandlerResolver.executeFindMessageHandlers") {
            executeFindMessageHandlers()
        }
    }

    fun findHandlersForType(qualifiedName: String, messageType: MessageType? = null): List<Handler> {
        return handlerCache.value
                .filter { messageType == null || it.handlerType.messageType == messageType }
                .filter { areAssignable(project, it.payload, qualifiedName) }
                .filter { it.element.isValid }
    }

    fun findAllHandlers(): List<Handler> = handlerCache.value

    fun findHandlerByElement(psiElement: PsiElement): Handler? {
        return handlerCache.value.firstOrNull { it.element == psiElement }
    }

    private fun executeFindMessageHandlers(): List<Handler> {
        return searchers
                .flatMap { it.search(project) }
                .distinct()
    }
}

