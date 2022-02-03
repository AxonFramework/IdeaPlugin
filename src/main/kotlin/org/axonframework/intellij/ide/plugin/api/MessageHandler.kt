package org.axonframework.intellij.ide.plugin.api

import com.intellij.psi.PsiMethod

data class MessageHandler(
        val element: PsiMethod,
        val handlerType: MessageHandlerType,
        val payloadType: String,
        val processingGroup: String?,
)
