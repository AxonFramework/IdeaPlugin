package org.axonframework.intellij.ide.plugin.api

import com.intellij.psi.PsiElement

/**
 * Parent interface of any creator or handler. Represents a wrapped PsiElement.
 *
 * @see Handler
 * @see MessageCreator
 */
interface PsiElementWrapper {
    val element: PsiElement
}
