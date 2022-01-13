package org.axonframework.intellij.ide.plugin.markers

import com.intellij.ide.util.PsiElementListCellRenderer
import com.intellij.openapi.util.Iconable.ICON_FLAG_READ_STATUS
import com.intellij.openapi.util.Iconable.ICON_FLAG_VISIBILITY
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.axonframework.intellij.ide.plugin.search.renderElementContainerText
import org.axonframework.intellij.ide.plugin.search.renderElementText
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
            return AxonIcons.Handler
        }
        return AxonIcons.Publisher
    }


}
