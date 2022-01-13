package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiNewExpression
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver

class JavaPublishMethodLineMarker : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is PsiIdentifier || element.parent.parent !is PsiNewExpression) {
            return null
        }
        val parent = element.parent.parent as PsiNewExpression

        val repository = element.project.getService(MessageHandlerResolver::class.java)
        val handlers = repository.findHandlersForType(parent.type!!)
        if (handlers.isEmpty()) {
            return null
        }
        return NavigationGutterIconBuilder.create(AxonIcons.Publisher)
                .setTooltipText("Navigate to Axon handlers")
                .setCellRenderer(AxonCellRenderer.getInstance())
                .setTargets(NotNullLazyValue.createValue { handlers.map { it.element } })
                .createLineMarkerInfo(element)
    }
}
