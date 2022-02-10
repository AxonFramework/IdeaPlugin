package org.axonframework.intellij.ide.plugin.api

import com.intellij.psi.PsiElement
import javax.swing.Icon

/**
 * Represents an element that creates a message (payload).
 *
 * @see org.axonframework.intellij.ide.plugin.resolving.MessageCreationResolver
 */
interface MessageCreator : PsiElementWrapper {
    /**
     * The PsiElement that is creating the payload.
     */
    override val element: PsiElement

    /**
     * Fully qualified name of the payload being created.
     */
    val payload: String

    /**
     * The parent handler that published the message. For example, if this MessageCreator represents an event
     * created by a CommandHandler, the parentHandler will be that CommandHandler.
     * The same applied for commands created by a SagaEventHandler, among others.
     */
    val parentHandler: Handler?

    /**
     * Renders the grey text next to the initial identifier.
     *
     * @return Container text used in a line marker popup.
     */
    fun renderContainerText(): String?

    /**
     * Returns the correct icon for the creator, based on the parent handler type.
     *
     * @return The correct icon to be used in a line marker popup
     */
    fun getIcon(): Icon
}

