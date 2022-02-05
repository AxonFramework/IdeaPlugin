package org.axonframework.intellij.ide.plugin.handlers.types

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.Handler

data class EventSourcingHandler(
        override val element: PsiMethod,
        override val payloadFullyQualifiedName: String,
        val model: String
) : Handler {
    override fun renderContainerText(): String {
        return model
    }

    override fun getIcon() = AxonIcons.Aggregate
}
