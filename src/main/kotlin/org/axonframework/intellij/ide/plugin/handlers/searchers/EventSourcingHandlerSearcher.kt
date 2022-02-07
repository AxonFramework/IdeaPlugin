package org.axonframework.intellij.ide.plugin.handlers.searchers

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.handlers.types.EventSourcingHandler
import org.axonframework.intellij.ide.plugin.util.containingClassname
import org.axonframework.intellij.ide.plugin.util.resolvePayloadType
import org.axonframework.intellij.ide.plugin.util.toQualifiedName
/**
 * Searches for any event handlers in aggregates that source the state of the aggregates.
 *
 * @see org.axonframework.intellij.ide.plugin.handlers.types.EventSourcingHandler
 */
class EventSourcingHandlerSearcher : AbstractHandlerSearcher(MessageHandlerType.EVENT_SOURCING) {
    override fun createMessageHandler(method: PsiMethod): Handler? {
        val payloadType = method.resolvePayloadType()?.toQualifiedName() ?: return null
        return EventSourcingHandler(method, payloadType, method.containingClassname())
    }
}
