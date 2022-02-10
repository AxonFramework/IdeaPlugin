package org.axonframework.intellij.ide.plugin.handlers.types

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType

/**
 * Represents a method being able to handle an event. There are more specific event handlers (`EventSourcingHandler` and
 * `SagaEventHandler`) that are not included here. These have their own representation, despite being meta-annotated
 * by `@EventHandler`
 *
 * @param processingGroup The name of the component handling the event, based on package or ProcessingGroup annotation
 *
 * @See org.axonframework.intellij.ide.plugin.handlers.searchers.EventHandlerSearcher
 * @see SagaEventHandler
 * @see EventSourcingHandler
 */
data class EventHandler(
        override val element: PsiMethod,
        override val payload: String,
        val processingGroup: String,
) : Handler {
    override val handlerType: MessageHandlerType = MessageHandlerType.EVENT

    override fun renderContainerText(): String {
        return processingGroup
    }

    override fun getIcon() = AxonIcons.Handler
}
