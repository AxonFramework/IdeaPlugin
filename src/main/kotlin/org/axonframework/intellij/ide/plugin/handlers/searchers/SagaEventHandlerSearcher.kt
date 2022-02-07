package org.axonframework.intellij.ide.plugin.handlers.searchers

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.handlers.types.SagaEventHandler
import org.axonframework.intellij.ide.plugin.util.findProcessingGroup
import org.axonframework.intellij.ide.plugin.util.resolvePayloadType
import org.axonframework.intellij.ide.plugin.util.toQualifiedName

/**
 * Searches for any saga event handlers.
 *
 * @see org.axonframework.intellij.ide.plugin.handlers.types.SagaEventHandler
 */
class SagaEventHandlerSearcher : AbstractHandlerSearcher(MessageHandlerType.SAGA) {
    override fun createMessageHandler(method: PsiMethod): Handler? {
        val payloadType = method.resolvePayloadType()?.toQualifiedName() ?: return null
        return SagaEventHandler(method, payloadType, method.findProcessingGroup())
    }
}
