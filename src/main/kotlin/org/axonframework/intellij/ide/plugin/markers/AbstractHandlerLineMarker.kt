package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.AxonIcons
import org.axonframework.intellij.ide.plugin.resolving.MessageCreationResolver
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.axonframework.intellij.ide.plugin.util.sortingByDisplayName

abstract class AbstractHandlerLineMarker : LineMarkerProvider {
    protected fun createLineMarker(element: PsiElement, psiMethod: PsiMethod): RelatedItemLineMarkerInfo<PsiElement>? {
        val repository = element.project.getService(MessageHandlerResolver::class.java)
        val handler = repository.findHandlerByElement(psiMethod) ?: return null

        return createLineMarker(element, handler.payloadFullyQualifiedName)
    }

    protected fun createLineMarker(element: PsiElement, qualifiedName: String): RelatedItemLineMarkerInfo<PsiElement>? {
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
