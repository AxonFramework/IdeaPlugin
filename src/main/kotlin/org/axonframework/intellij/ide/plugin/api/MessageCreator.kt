package org.axonframework.intellij.ide.plugin.api

import com.intellij.psi.PsiElement
import javax.swing.Icon

/**
 * Represents an element that creates a message (payload).
 *
 * @see org.axonframework.intellij.ide.plugin.resolving.MessageCreationResolver
 */
interface MessageCreator : PsiElementWrapper {
    override val element: PsiElement
    val payloadFullyQualifiedName: String
    val parentHandler: Handler?


    fun renderContainerText(): String?
    fun getIcon(): Icon
}

