package org.axonframework.intellij.ide.plugin.creators

import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.api.MessageCreator

data class DefaultMessageCreator(override val element: PsiElement, override val payloadFullyQualifiedName: String) : MessageCreator
