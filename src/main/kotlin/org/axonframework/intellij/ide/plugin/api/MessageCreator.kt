package org.axonframework.intellij.ide.plugin.api

import com.intellij.psi.PsiElement
import javax.swing.Icon

interface MessageCreator : PsiElementWrapper {
    override val element: PsiElement
    val payloadFullyQualifiedName: String
    val parentHandler: Handler?


    fun renderContainerText(): String?
    fun getIcon(): Icon
}

