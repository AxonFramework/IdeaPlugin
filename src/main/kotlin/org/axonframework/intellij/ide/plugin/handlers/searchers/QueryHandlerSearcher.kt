package org.axonframework.intellij.ide.plugin.handlers.searchers

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.HandlerSearcher
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.handlers.types.QueryHandler
import org.axonframework.intellij.ide.plugin.util.findProcessingGroup
import org.axonframework.intellij.ide.plugin.util.toQualifiedName

class QueryHandlerSearcher : HandlerSearcher(MessageHandlerType.QUERY) {
    override fun createMessageHandler(method: PsiMethod): Handler? {
        val payloadType = resolvePayloadType(method)?.toQualifiedName() ?: return null
        return QueryHandler(method, payloadType, method.findProcessingGroup())
    }
}
