package org.axonframework.intellij.ide.plugin.handlers.types

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType

/**
 * Represents a method being able to handle an event for sourcing an aggregate.
 *
 * @param model The fully qualified name of the aggregate class sourced by the event
 * @See org.axonframework.intellij.ide.plugin.handlers.searchers.EventSourcingHandlerSearcher
 */
data class EventSourcingHandler(
        override val element: PsiMethod,
        override val payload: String,
        val model: String,
) : Handler {
    override val handlerType: MessageHandlerType = MessageHandlerType.EVENT_SOURCING

    override fun renderContainerText(): String {
        return model
    }

    override fun getIcon() = AxonIcons.Model
}
