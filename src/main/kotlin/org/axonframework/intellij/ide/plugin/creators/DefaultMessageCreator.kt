package org.axonframework.intellij.ide.plugin.creators

import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.Handler
import org.axonframework.intellij.ide.plugin.api.MessageCreator
import org.axonframework.intellij.ide.plugin.handlers.types.CommandHandler
import org.axonframework.intellij.ide.plugin.handlers.types.EventSourcingHandler
import org.axonframework.intellij.ide.plugin.handlers.types.SagaEventHandler
import javax.swing.Icon

/**
 * Default implementation of a `MessageCreator`, handling the container text and icons to be shown for it.
 *
 * If MessageCreators diverge in the future (e.g. require too different functionality), other implementations can be
 * made and constructed in the `MessageCreationResolver`
 *
 * @see MessageCreator
 * @see org.axonframework.intellij.ide.plugin.resolving.MessageCreationResolver
 */
data class DefaultMessageCreator(override val element: PsiElement, override val payload: String, override val parentHandler: Handler?) : MessageCreator {
    /**
     * Renders the grey text next to the initial identifier.
     *
     * If the parent handler is EventSourcingHandler, it means that the message is published from the aggregate while
     * an event is being applied to the source. We show a warning here that it's a side effect of it.
     *
     * If the parent handler is a Saga, add the Saga to qualify the event better.
     *
     * @return Container text used in a line marker popup.
     */
    override fun renderContainerText(): String? {
        if (parentHandler is EventSourcingHandler) {
            return "Side effect of EventSourcingHandler"
        }
        if (parentHandler is SagaEventHandler) {
            return "Saga ${parentHandler.processingGroup}"
        }
        return null
    }

    /**
     * Returns the correct icon for the creator, based on the parent handler type.
     *
     * @return The correct icon to be used in a line marker popup
     */
    override fun getIcon(): Icon {
        if (parentHandler is CommandHandler || parentHandler is EventSourcingHandler) {
            return AxonIcons.Model
        }
        if (parentHandler is SagaEventHandler) {
            return AxonIcons.Saga
        }
        return AxonIcons.Publisher
    }
}
