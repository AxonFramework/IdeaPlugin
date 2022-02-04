package org.axonframework.intellij.ide.plugin.creators

import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageCreator
import org.axonframework.intellij.ide.plugin.handlers.types.CommandHandler
import org.axonframework.intellij.ide.plugin.handlers.types.EventSourcingHandler
import org.axonframework.intellij.ide.plugin.handlers.types.SagaEventHandler
import javax.swing.Icon

data class DefaultMessageCreator(override val element: PsiElement, override val payloadFullyQualifiedName: String, override val parentHandler: Handler?) : MessageCreator {
    override fun renderContainerText(): String? {
        if (parentHandler is EventSourcingHandler) {
            return "Side effect of EventSourcingHandler"
        }
        if (parentHandler is SagaEventHandler) {
            return "Saga ${parentHandler.processingGroup}"
        }
        return null
    }

    override fun getIcon(): Icon {
        if (parentHandler is CommandHandler || parentHandler is EventSourcingHandler) {
            return AxonIcons.Aggregate
        }
        if (parentHandler is SagaEventHandler) {
            return AxonIcons.Saga
        }
        return AxonIcons.Publisher
    }
}
