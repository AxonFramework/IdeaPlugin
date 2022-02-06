package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.util.castSafelyTo
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.resolving.MessageCreationResolver
import org.axonframework.intellij.ide.plugin.util.sortingByDisplayName
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.getUParentForIdentifier

/**
 * Provides a gutter icon on constructor invocations when that type is also known as a message paylaod
 */
class HandlerMethodLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val uElement = getUParentForIdentifier(element) ?: return null
        val method = uElement.castSafelyTo<UMethod>() ?: return null
        val qualifiedName = method.uastParameters.getOrNull(0)?.typeReference?.getQualifiedName() ?: return null

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
