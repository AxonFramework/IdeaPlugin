package org.axonframework.intellij.ide.plugin.handlers.types

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import javax.swing.Icon

data class CommandHandlerInterceptor(
        override val element: PsiMethod,
        override val payloadFullyQualifiedName: String,
        val aggregate: String,
        override val handlerType: MessageHandlerType = MessageHandlerType.COMMAND_INTERCEPTOR
) : Handler {
    override fun renderText(): String {
        return "Command Interceptor of $aggregate"
    }

    override fun renderContainerText(): String {
        return aggregate
    }

    override fun getIcon(): Icon {
        return AxonIcons.Interceptor
    }
}
