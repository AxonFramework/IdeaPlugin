package org.axonframework.intellij.ide.plugin.handlers.types

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import javax.swing.Icon

/**
 * Represents a method that is able to intercept a command.
 *
 * @param componentName The fully qualified name of the class intercepting the command
 * @see org.axonframework.intellij.ide.plugin.handlers.searchers.CommandHandlerInterceptorSearcher
 */
data class CommandHandlerInterceptor(
        override val element: PsiMethod,
        override val payload: String,
        val componentName: String,
) : Handler {
    override val handlerType: MessageHandlerType = MessageHandlerType.COMMAND_INTERCEPTOR

    override fun renderText(): String {
        return "Command Interceptor of $componentName"
    }

    override fun renderContainerText(): String {
        return componentName
    }

    override fun getIcon(): Icon {
        return AxonIcons.Interceptor
    }
}
