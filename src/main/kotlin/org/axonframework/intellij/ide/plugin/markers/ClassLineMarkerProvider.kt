package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.resolving.MessageCreationResolver
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.axonframework.intellij.ide.plugin.util.isAggregate
import org.axonframework.intellij.ide.plugin.util.sortingByDisplayName
import org.jetbrains.uast.UClass
import org.jetbrains.uast.getUParentForIdentifier

/**
 * Provides a gutter icon on class declarations of types which are used in handlers.
 */
class ClassLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val uElement = getUParentForIdentifier(element) ?: return null
        if (uElement !is UClass || uElement.isAggregate()) {
            return null
        }

        val qualifiedName = uElement.qualifiedName ?: return null
        val handlers = element.project.getService(MessageHandlerResolver::class.java).findHandlersForType(qualifiedName)
        if (handlers.isEmpty()) {
            return null
        }
        val publishers = element.project.getService(MessageCreationResolver::class.java).getCreatorsForPayload(qualifiedName)

        val items = (handlers + publishers).sortedWith(element.project.sortingByDisplayName()).map { it.element }
        return NavigationGutterIconBuilder.create(AxonIcons.Publisher)
                .setTooltipText("Navigate to message handlers and creations")
                .setCellRenderer(AxonCellRenderer.getInstance())
                .setTargets(items)
                .setAlignment(GutterIconRenderer.Alignment.LEFT)
                .createLineMarkerInfo(element)
    }
}
