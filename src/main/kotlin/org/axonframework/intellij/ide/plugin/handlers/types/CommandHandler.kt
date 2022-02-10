package org.axonframework.intellij.ide.plugin.handlers.types

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.util.toShortName
import javax.swing.Icon

/**
 * Represents a method being able to handle a command.
 *
 * @param componentName The fully qualified name of the class handling the command.
 * @See org.axonframework.intellij.ide.plugin.handlers.searchers.CommandHandlerSearcher
 */
data class CommandHandler(
        override val element: PsiMethod,
        override val payload: String,
        val componentName: String,
) : Handler {
    override val handlerType: MessageHandlerType = MessageHandlerType.COMMAND
    override fun renderContainerText(): String {
        return componentName.toShortName()
    }

    override fun getIcon(): Icon {
        return AxonIcons.Model
    }
}
