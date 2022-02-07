package org.axonframework.intellij.ide.plugin.api

import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.util.toShortName
import javax.swing.Icon

/**
 * Parent interface of any Handler, providing methods to describe the handler in interface elements.
 *
 * @see org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
 * @see HandlerSearcher
 */
interface Handler : PsiElementWrapper {
    override val element: PsiElement
    val handlerType: MessageHandlerType
    val payloadFullyQualifiedName: String

    fun renderText(): String = payloadFullyQualifiedName.toShortName()
    fun renderContainerText(): String?
    fun getIcon(): Icon
}
