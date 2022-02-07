package org.axonframework.intellij.ide.plugin.markers

import com.intellij.ide.util.PsiElementListCellRenderer
import com.intellij.openapi.util.Iconable.ICON_FLAG_VISIBILITY
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.resolving.MessageCreationResolver
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.axonframework.intellij.ide.plugin.util.containingClassname
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.toUElement
import javax.swing.Icon

class AxonCellRenderer : PsiElementListCellRenderer<PsiElement>() {
    companion object {
        private val renderer = lazy { AxonCellRenderer() }
        fun getInstance() = renderer.value
    }

    override fun getElementText(element: PsiElement): String {
        val handlerResolver = element.project.getService(MessageHandlerResolver::class.java)
        val handler = handlerResolver.findHandlerByElement(element)
        if (handler != null) {
            return handler.payloadFullyQualifiedName.split(".").last()
        }

        val creatorResolver = element.project.getService(MessageCreationResolver::class.java)
        val creator = creatorResolver.findCreatorByElement(element)
        if (creator?.parentHandler != null) {
            return creator.parentHandler!!.renderText()
        }

        val methodParent = element.toUElement()?.getParentOfType<UMethod>()
        if (methodParent != null) {
            return methodParent.containingClassname() + "." + methodParent.name
        }

        return element.containingFile.name
    }

    /**
     * Renders the container text in the line marker popup. Contains additional contextual information.
     *
     * @return PSI element container text
     */
    override fun getContainerText(element: PsiElement, name: String?): String? {
        val handlerResolver = element.project.getService(MessageHandlerResolver::class.java)

        val handler = handlerResolver.findHandlerByElement(element)
        if (handler != null) {
            return handler.renderContainerText()
        }

        val creatorResolver = element.project.getService(MessageCreationResolver::class.java)
        val creator = creatorResolver.findCreatorByElement(element)
        if (creator != null) {
            return creator.renderContainerText()
        }
        return null
    }

    override fun getIconFlags(): Int {
        return ICON_FLAG_VISIBILITY
    }

    override fun getIcon(element: PsiElement): Icon {
        val handler = element.project.getService(MessageHandlerResolver::class.java).findHandlerByElement(element)
        if (handler != null) {
            return handler.getIcon()
        }
        val creator = element.project.getService(MessageCreationResolver::class.java).findCreatorByElement(element)
        if(creator != null) {
            return creator.parentHandler?.getIcon() ?: creator.getIcon()
        }
        return AxonIcons.Publisher
    }
}
