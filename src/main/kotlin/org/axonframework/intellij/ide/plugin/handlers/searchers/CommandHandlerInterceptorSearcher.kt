package org.axonframework.intellij.ide.plugin.handlers.searchers

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.HandlerSearcher
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.handlers.types.CommandHandlerInterceptor
import org.axonframework.intellij.ide.plugin.util.containingClassname
import org.axonframework.intellij.ide.plugin.util.resolvePayloadType
import org.axonframework.intellij.ide.plugin.util.toQualifiedName

class CommandHandlerInterceptorSearcher : HandlerSearcher(MessageHandlerType.COMMAND_INTERCEPTOR) {
    override fun createMessageHandler(method: PsiMethod): Handler? {
        val payloadType = method.resolvePayloadType()?.toQualifiedName() ?: return null
        return CommandHandlerInterceptor(method, payloadType, method.containingClassname())
    }
}
