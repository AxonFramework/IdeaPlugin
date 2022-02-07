package org.axonframework.intellij.ide.plugin.handlers.types

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType

data class EventProcessorHandler(
        override val element: PsiMethod,
        override val payloadFullyQualifiedName: String,
        val processingGroup: String,
        override val handlerType: MessageHandlerType = MessageHandlerType.EVENT
) : Handler {
    override fun renderContainerText(): String {
        return processingGroup
    }

    override fun getIcon() = AxonIcons.Handler
}
