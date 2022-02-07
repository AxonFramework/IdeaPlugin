package org.axonframework.intellij.ide.plugin.handlers.types

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import javax.swing.Icon

data class CommandHandler(
        override val element: PsiMethod,
        override val payloadFullyQualifiedName: String,
        val model: String,
        val modelFqn: String,
        override val handlerType: MessageHandlerType = MessageHandlerType.COMMAND
) : Handler {
    override fun renderContainerText(): String {
        return model
    }

    override fun getIcon(): Icon {
        return AxonIcons.Model
    }
}
