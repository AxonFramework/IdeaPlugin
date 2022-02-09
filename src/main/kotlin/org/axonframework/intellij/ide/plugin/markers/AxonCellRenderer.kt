package org.axonframework.intellij.ide.plugin.markers

import com.intellij.ide.util.PsiElementListCellRenderer
import com.intellij.openapi.util.Iconable.ICON_FLAG_VISIBILITY
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.util.toContainerText
import org.axonframework.intellij.ide.plugin.util.toElementText
import org.axonframework.intellij.ide.plugin.util.toIcon
import javax.swing.Icon

class AxonCellRenderer : PsiElementListCellRenderer<PsiElement>() {
    companion object {
        private val renderer = lazy { AxonCellRenderer() }
        fun getInstance() = renderer.value
    }

    override fun getElementText(element: PsiElement): String = element.toElementText()

    /**
     * Renders the container text in the line marker popup. Contains additional contextual information.
     *
     * @return PSI element container text
     */
    override fun getContainerText(element: PsiElement, name: String?): String? = element.toContainerText()

    override fun getIconFlags(): Int {
        return ICON_FLAG_VISIBILITY
    }

    override fun getIcon(element: PsiElement): Icon = element.toIcon()
}
