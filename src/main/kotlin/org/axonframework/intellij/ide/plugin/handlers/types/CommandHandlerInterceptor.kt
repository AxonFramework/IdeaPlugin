package org.axonframework.intellij.ide.plugin.handlers.types

import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.api.Handler
import javax.swing.Icon

data class CommandHandlerInterceptor(
        override val element: PsiMethod,
        override val payloadFullyQualifiedName: String,
        val aggregate: String
) : Handler {
    override fun renderContainerText(): String {
        return aggregate
    }

    override fun getIcon(): Icon {
        return AxonIcons.Bean
    }
}
