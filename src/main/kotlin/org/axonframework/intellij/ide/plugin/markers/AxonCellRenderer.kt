package org.axonframework.intellij.ide.plugin.markers

import com.intellij.ide.util.PsiElementListCellRenderer
import com.intellij.openapi.util.Iconable.ICON_FLAG_VISIBILITY
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.axonframework.intellij.ide.plugin.AxonIcons
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
        return ICON_FLAG_VISIBILITY
    }

    override fun getIcon(element: PsiElement): Icon {
        val handler = element.project.getService(MessageHandlerResolver::class.java).findHandlerByElement(element)
        if (handler != null) {
            return handler.getIcon()
        }
        return determineCreatorIcon(element)
    }

    private fun determineCreatorIcon(element: PsiElement): Icon {
        val clazzParent = PsiTreeUtil.findFirstParent(element, true) { it is PsiClass } as PsiClass?
        if (clazzParent != null) {
            val isInSaga = clazzParent.annotations.any { it.hasQualifiedName("org.axonframework.spring.stereotype.Saga")}
            if(isInSaga) {
                return AxonIcons.Saga
            }
            if(isAggregateInstance(clazzParent)) {
                return AxonIcons.Aggregate
            }
        }
        return AxonIcons.Publisher
    }

    private fun isAggregateInstance(clazzParent: PsiClass): Boolean {
        val isAggregate = clazzParent.annotations.any { it.hasQualifiedName("org.axonframework.spring.stereotype.Aggregate")}
        if(isAggregate) {
            return true
        }
        val isEntity = clazzParent.allFields.any { it.hasAnnotation("org.axonframework.modelling.command.EntityId") }
        if(isEntity) {
            return true
        }

        return false
    }
}
