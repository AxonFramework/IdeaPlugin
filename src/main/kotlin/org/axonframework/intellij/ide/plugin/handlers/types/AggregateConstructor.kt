package org.axonframework.intellij.ide.plugin.handlers.types

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.util.toShortName
import javax.swing.Icon

data class AggregateConstructor(
        override val element: PsiMethod,
        override val payloadFullyQualifiedName: String,
        override val handlerType: MessageHandlerType = MessageHandlerType.COMMAND
) : Handler {
    override fun renderContainerText(): String {
        return payloadFullyQualifiedName.toShortName()
    }

    override fun getIcon(): Icon {
        return AxonIcons.Model
    }
}
