package org.axonframework.intellij.ide.plugin.handlers.searchers

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.HandlerSearcher
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.handlers.types.CommandHandler
import org.axonframework.intellij.ide.plugin.util.containingClassname
import org.axonframework.intellij.ide.plugin.util.toQualifiedName

class CommandHandlerSearcher : HandlerSearcher(MessageHandlerType.COMMAND) {
    override fun createMessageHandler(method: PsiMethod): Handler? {
        val payloadType = resolvePayloadType(method)?.toQualifiedName() ?: return null
        return CommandHandler(method, payloadType, method.containingClassname())
    }
}
