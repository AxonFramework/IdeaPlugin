package org.axonframework.intellij.ide.plugin.api

import com.intellij.psi.PsiElement
import javax.swing.Icon

/**
 * Parent interface of any Handler, providing methods to describe the handler in interface elements.
 */
interface Handler : PsiElementWrapper {
    override val element: PsiElement
    val payloadFullyQualifiedName: String

    fun renderContainerText(): String?
    fun getIcon(): Icon
}
