package org.axonframework.intellij.ide.plugin.api

import com.intellij.psi.PsiElement

interface MessageCreator : PsiElementWrapper {
    override val element: PsiElement
    val payloadFullyQualifiedName: String
}

