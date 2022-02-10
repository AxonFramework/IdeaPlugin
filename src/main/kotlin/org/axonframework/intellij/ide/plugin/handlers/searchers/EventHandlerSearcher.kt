package org.axonframework.intellij.ide.plugin.handlers.searchers

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.handlers.types.EventHandler
import org.axonframework.intellij.ide.plugin.util.findProcessingGroup
import org.axonframework.intellij.ide.plugin.util.resolvePayloadType
import org.axonframework.intellij.ide.plugin.util.toQualifiedName

/**
 * Searches for any event handlers that are not part of the aggregate model.
 *
 * @see org.axonframework.intellij.ide.plugin.handlers.types.EventHandler
 */
class EventHandlerSearcher : AbstractHandlerSearcher(MessageHandlerType.EVENT) {
    override fun createMessageHandler(method: PsiMethod): Handler? {
        val payloadType = method.resolvePayloadType()?.toQualifiedName() ?: return null
        return EventHandler(method, payloadType, method.findProcessingGroup())
    }
}
