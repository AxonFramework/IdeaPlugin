package org.axonframework.intellij.ide.plugin.markers

import com.intellij.ide.util.PsiElementListCellRenderer
import com.intellij.openapi.util.Iconable.ICON_FLAG_READ_STATUS
import com.intellij.openapi.util.Iconable.ICON_FLAG_VISIBILITY
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.axonframework.intellij.ide.plugin.api.MessageHandler
import org.axonframework.intellij.ide.plugin.api.MessageHandlerType
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.axonframework.intellij.ide.plugin.util.renderElementContainerText
import org.axonframework.intellij.ide.plugin.util.renderElementText
import javax.swing.Icon

class AxonCellRenderer : PsiElementListCellRenderer<PsiElement>() {
    companion object {
        private val renderer = lazy { AxonCellRenderer() }
        fun getInstance() = renderer.value
    }

    override fun getElementText(element: PsiElement): String {
        return renderElementText(element)
    }

    override fun getContainerText(element: PsiElement, name: String?): String? {
        return renderElementContainerText(element)
    }

    override fun getIconFlags(): Int {
        return ICON_FLAG_VISIBILITY + ICON_FLAG_READ_STATUS
    }

    override fun getIcon(element: PsiElement): Icon {
        val handler = element.project.getService(MessageHandlerResolver::class.java).findHandlerByElement(element)
        if (handler != null) {
            return iconForHandler(handler)
        }
        val clazzParent = PsiTreeUtil.findFirstParent(element, true) { it is PsiClass } as PsiClass?
        if (clazzParent != null && clazzParent.annotations.any { it.hasQualifiedName("org.axonframework.spring.stereotype.Saga") }) {
            return AxonIcons.Saga
        }
        return AxonIcons.Publisher
    }

    private fun iconForHandler(handler: MessageHandler): Icon {
        return when (handler.handlerType) {
            MessageHandlerType.SAGA -> AxonIcons.Saga
            MessageHandlerType.EVENT_SOURCING -> AxonIcons.Aggregate
            MessageHandlerType.COMMAND -> AxonIcons.Aggregate
            MessageHandlerType.COMMAND_INTERCEPTOR -> AxonIcons.Aggregate
            else -> AxonIcons.Handler
        }
    }


}
