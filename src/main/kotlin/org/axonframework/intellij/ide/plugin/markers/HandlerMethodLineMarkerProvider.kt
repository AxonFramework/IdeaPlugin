package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.resolving.MessageCreationResolver
import org.axonframework.intellij.ide.plugin.util.containingClassFqn
import org.axonframework.intellij.ide.plugin.util.sortingByDisplayName
import org.jetbrains.uast.UIdentifier
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElement

/**
 * Provides a gutter icon on constructor invocations when that type is also known as a message paylaod
 */
class HandlerMethodLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element.toUElement() !is UIdentifier) {
            return null
        }
        val uElement = element.parent.toUElement()
        if (uElement !is UMethod) {
            return null
        }
        val qualifiedName = if (uElement.isConstructor && uElement.uastParameters.isNotEmpty()) {
            uElement.containingClassFqn()
        } else uElement.uastParameters.getOrNull(0)?.typeReference?.getQualifiedName() ?: return null

        val publisherResolver = element.project.getService(MessageCreationResolver::class.java)
        val resolvers = publisherResolver.getCreatorsForPayload(qualifiedName)
                .sortedWith(element.project.sortingByDisplayName())
        if (resolvers.isEmpty()) {
            return null
        }

        return NavigationGutterIconBuilder.create(AxonIcons.Handler)
                .setTooltipText("Navigate to creation of message payload")
                .setCellRenderer(AxonCellRenderer.getInstance())
                .setTargets(resolvers.map { it.element })
                .setAlignment(GutterIconRenderer.Alignment.LEFT)
                .createLineMarkerInfo(element)
    }
}
