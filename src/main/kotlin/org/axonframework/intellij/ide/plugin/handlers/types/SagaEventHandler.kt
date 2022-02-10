package org.axonframework.intellij.ide.plugin.handlers.types

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType

/**
 * Represents a method in a Saga that is able to handle an event
 *
 * @param processingGroup The name of the saga handling the event, based on package or ProcessingGroup annotation
 * @See org.axonframework.intellij.ide.plugin.handlers.searchers.SagaEventHandlerSearcher
 */
data class SagaEventHandler(
        override val element: PsiMethod,
        override val payload: String,
        val processingGroup: String,
) : Handler {
    override val handlerType: MessageHandlerType = MessageHandlerType.SAGA

    override fun renderContainerText(): String {
        return "Saga: $processingGroup"
    }

    override fun getIcon() = AxonIcons.Saga
}
