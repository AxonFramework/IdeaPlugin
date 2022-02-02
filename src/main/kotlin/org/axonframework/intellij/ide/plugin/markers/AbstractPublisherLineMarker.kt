package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiType
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver

abstract class AbstractPublisherLineMarker : LineMarkerProvider {
    protected fun createLineMarker(element: PsiElement, psiType: PsiType): RelatedItemLineMarkerInfo<PsiElement>? {
        val repository = element.project.getService(MessageHandlerResolver::class.java)
        val handlers = repository.findHandlersForType(psiType)
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
