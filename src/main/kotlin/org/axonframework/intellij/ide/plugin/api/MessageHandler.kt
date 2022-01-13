package org.axonframework.intellij.ide.plugin.api

import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType

data class MessageHandler(
        val element: PsiMethod,
        val messageType: MessageHandlerType,
        val payloadType: PsiType,
        val processingGroup: String?,
)
