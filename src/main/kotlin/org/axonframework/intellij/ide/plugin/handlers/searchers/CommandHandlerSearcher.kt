package org.axonframework.intellij.ide.plugin.handlers.searchers

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.handlers.types.CommandHandler
import org.axonframework.intellij.ide.plugin.util.containingClassFqn
import org.axonframework.intellij.ide.plugin.util.containingClassname
import org.axonframework.intellij.ide.plugin.util.resolvePayloadType
import org.axonframework.intellij.ide.plugin.util.toQualifiedName
/**
 * Searches for any command handlers
 *
 * @see org.axonframework.intellij.ide.plugin.handlers.types.CommandHandler
 */
class CommandHandlerSearcher : AbstractHandlerSearcher(MessageHandlerType.COMMAND) {
    override fun createMessageHandler(method: PsiMethod): Handler? {
        val payloadType = method.resolvePayloadType()?.toQualifiedName() ?: return null
        return CommandHandler(method, payloadType, method.containingClassname(), method.containingClassFqn())
    }
}
