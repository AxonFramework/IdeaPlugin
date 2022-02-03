package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.axonframework.intellij.ide.plugin.resolving.MessagePublisherResolver

abstract class AbstractHandlerLineMarker : LineMarkerProvider {
    protected fun createLineMarker(element: PsiElement, psiMethod: PsiMethod): RelatedItemLineMarkerInfo<PsiElement>? {
        val repository = element.project.getService(MessageHandlerResolver::class.java)
        val handler = repository.findHandlerByElement(psiMethod) ?: return null

        return createLineMarker(element, handler.payloadType)
    }

    protected fun createLineMarker(element: PsiElement, qualifiedName: String): RelatedItemLineMarkerInfo<PsiElement>? {
        val publisherResolver = element.project.getService(MessagePublisherResolver::class.java)
        val resolvers = publisherResolver.getConstructorPublishersForType(qualifiedName)
        if (resolvers.isEmpty()) {
            return null
        }
        return NavigationGutterIconBuilder.create(AxonIcons.Handler)
                .setTooltipText("Navigate to publishers")
                .setCellRenderer(AxonCellRenderer.getInstance())
                .setTargets(resolvers)
                .createLineMarkerInfo(element)
    }
}
