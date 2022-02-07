package org.axonframework.intellij.ide.plugin.handlers.searchers

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.handlers.types.CommandHandlerInterceptor
import org.axonframework.intellij.ide.plugin.util.containingClassname
import org.axonframework.intellij.ide.plugin.util.resolvePayloadType
import org.axonframework.intellij.ide.plugin.util.toQualifiedName
/**
 * Searches for any command interceptors.
 *
 * @see org.axonframework.intellij.ide.plugin.handlers.types.CommandHandlerInterceptor
 */
class CommandHandlerInterceptorSearcher : AbstractHandlerSearcher(MessageHandlerType.COMMAND_INTERCEPTOR) {
    override fun createMessageHandler(method: PsiMethod): Handler? {
        val payloadType = method.resolvePayloadType()?.toQualifiedName() ?: return null
        return CommandHandlerInterceptor(method, payloadType, method.containingClassname())
    }
}
