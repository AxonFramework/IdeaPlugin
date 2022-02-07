package org.axonframework.intellij.ide.plugin.handlers.searchers

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.HandlerSearcher
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.handlers.types.EventSourcingHandler
import org.axonframework.intellij.ide.plugin.util.containingClassname
import org.axonframework.intellij.ide.plugin.util.resolvePayloadType
import org.axonframework.intellij.ide.plugin.util.toQualifiedName

class EventSourcingHandlerSearcher : HandlerSearcher(MessageHandlerType.EVENT_SOURCING) {
    override fun createMessageHandler(method: PsiMethod): Handler? {
        val payloadType = method.resolvePayloadType()?.toQualifiedName() ?: return null
        return EventSourcingHandler(method, payloadType, method.containingClassname())
    }
}
