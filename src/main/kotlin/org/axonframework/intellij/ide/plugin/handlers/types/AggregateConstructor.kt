package org.axonframework.intellij.ide.plugin.handlers.types

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.util.toShortName
import javax.swing.Icon

/**
 * Represents a constructor invocation of an Aggregate.
 * This is often done during command handling, where aggregate A creates an instance of aggregate B.
 *
 * @see org.axonframework.intellij.ide.plugin.handlers.searchers.AggregateConstructorSearcher
 */
data class AggregateConstructor(
        override val element: PsiMethod,
        override val payload: String,
) : Handler {
    override val handlerType: MessageHandlerType = MessageHandlerType.COMMAND

    override fun renderContainerText(): String {
        return payload.toShortName()
    }

    override fun getIcon(): Icon {
        return AxonIcons.Model
    }
}
