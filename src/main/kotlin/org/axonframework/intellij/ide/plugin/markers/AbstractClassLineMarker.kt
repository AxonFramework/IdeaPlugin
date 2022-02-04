package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.resolving.MessageCreationResolver
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.axonframework.intellij.ide.plugin.util.sortingByDisplayName

abstract class AbstractClassLineMarker : LineMarkerProvider {
    protected fun createLineMarker(element: PsiElement, qualifiedName: String): RelatedItemLineMarkerInfo<PsiElement>? {
        val handlers = element.project.getService(MessageHandlerResolver::class.java).findHandlersForType(qualifiedName)
        if (handlers.isEmpty()) {
            return null
        }
        val publishers = element.project.getService(MessageCreationResolver::class.java).getCreatorsForPayload(qualifiedName)

        val items = (handlers + publishers).sortedWith(element.project.sortingByDisplayName()).map { it.element }
        return NavigationGutterIconBuilder.create(AxonIcons.Both)
                .setTooltipText("Navigate to message handlers and creations")
                .setCellRenderer(AxonCellRenderer.getInstance())
                .setTargets(items)
                .setAlignment(GutterIconRenderer.Alignment.LEFT)
                .createLineMarkerInfo(element)

    }
}
