package org.axonframework.intellij.ide.plugin.handlers.searchers

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.HandlerSearcher
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.handlers.types.SagaEventHandler
import org.axonframework.intellij.ide.plugin.util.findProcessingGroup
import org.axonframework.intellij.ide.plugin.util.resolvePayloadType
import org.axonframework.intellij.ide.plugin.util.toQualifiedName

class SagaEventHandlerSearcher : HandlerSearcher(MessageHandlerType.SAGA) {
    override fun createMessageHandler(method: PsiMethod): Handler? {
        val payloadType = method.resolvePayloadType()?.toQualifiedName() ?: return null
        return SagaEventHandler(method, payloadType, method.findProcessingGroup())
    }
}
