package org.axonframework.intellij.ide.plugin.api

import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.util.toShortName
import javax.swing.Icon

/**
 * Parent interface of any Handler, providing methods to describe the handler in interface elements.
 *
 * @see org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
 * @see org.axonframework.intellij.ide.plugin.handlers.searchers.AbstractHandlerSearcher
 */
interface Handler : PsiElementWrapper {
    /**
     * The PsiElement of the handler.
     */
    override val element: PsiElement

    /**
     * The type of the handler, used for filtering based on the handler type.
     * @see MessageHandlerType
     */
    val handlerType: MessageHandlerType

    /**
     * Fully qualified name of the payload being created.
     */
    val payload: String

    /**
     * Renders the main text in line marker popups. By default, it just shows the name of the payload's class,
     * but can be overridden by specific handlers.
     */
    fun renderText(): String = payload.toShortName()

    /**
     * Renders the grey text next to the initial identifier. Is optional, and by default empty, but can be overridden
     * by specific handlers
     *
     * @return Container text used in a line marker popup.
     */
    fun renderContainerText(): String?

    /**
     * Returns the correct icon for the handler, should be implemented by each implementor of Handler.
     *
     * @return The correct icon to be used in a line marker popup
     */
    fun getIcon(): Icon
}
