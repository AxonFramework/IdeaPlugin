package org.axonframework.intellij.ide.plugin.handlers.types

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType

/**
 * Represents a method being able to handle a query.
 *
 * @param componentName The name of the component handling the event, based on package or ProcessingGroup annotation
 * @See org.axonframework.intellij.ide.plugin.handlers.searchers.QueryHandlerSearcher
 */
data class QueryHandler(
        override val element: PsiMethod,
        override val payload: String,
        val componentName: String,
) : Handler {
    override val handlerType: MessageHandlerType = MessageHandlerType.QUERY

    override fun renderContainerText(): String {
        return componentName
    }

    override fun getIcon() = AxonIcons.Handler
}
