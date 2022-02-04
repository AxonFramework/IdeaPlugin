package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver

abstract class AbstractPublisherLineMarker : LineMarkerProvider {
    protected fun createLineMarker(element: PsiElement, qualifiedName: String): RelatedItemLineMarkerInfo<PsiElement>? {
        val repository = element.project.getService(MessageHandlerResolver::class.java)
        val handlers = repository.findHandlersForType(qualifiedName)
        if (handlers.isEmpty()) {
            return null
        }
        return NavigationGutterIconBuilder.create(AxonIcons.Publisher)
                .setTooltipText("Navigate to Axon handlers")
                .setCellRenderer(AxonCellRenderer.getInstance())
                .setAlignment(GutterIconRenderer.Alignment.LEFT)
                .setTargets(NotNullLazyValue.createValue { handlers.map { it.element } })
                .createLineMarkerInfo(element)

    }
}
