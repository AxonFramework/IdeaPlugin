package org.axonframework.intellij.ide.plugin.markers

import com.intellij.ide.util.PsiElementListCellRenderer
import com.intellij.openapi.util.Iconable.ICON_FLAG_VISIBILITY
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.resolving.MessageCreationResolver
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import javax.swing.Icon

class AxonCellRenderer : PsiElementListCellRenderer<PsiElement>() {
    companion object {
        private val renderer = lazy { AxonCellRenderer() }
        fun getInstance() = renderer.value
    }

    override fun getElementText(element: PsiElement): String {
        val handlerResolver = element.project.getService(MessageHandlerResolver::class.java)
        val handler = handlerResolver.findHandlerByElement(element)
        if(handler != null) {
            return handler.payloadFullyQualifiedName.split(".").last()
        }

        val creatorResolver = element.project.getService(MessageCreationResolver::class.java)
        val creator = creatorResolver.findCreatorByElement(element)
        if(creator?.parentHandler != null) {
            return creator.parentHandler!!.payloadFullyQualifiedName.split(".").last()
        }

        val ktMethodParent = PsiTreeUtil.getParentOfType(element, KtNamedFunction::class.java)
        if (ktMethodParent != null) {
            return ktMethodParent.containingClass()?.name + "." + ktMethodParent.name
        }
        val javaMethodParent = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java)
        if (javaMethodParent != null) {
            return javaMethodParent.containingClass?.name + "." + javaMethodParent.name
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
            return creator.getIcon()
        }
        return AxonIcons.Publisher
    }
}
