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

        val publisherResolver = element.project.getService(MessagePublisherResolver::class.java)
        val resolvers = publisherResolver.getConstructorPublishersForType(handler.payloadType)
        return NavigationGutterIconBuilder.create(AxonIcons.Handler)
                .setTooltipText("Navigate to ${handler.messageType.displayName()} constructor")
                .setCellRenderer(AxonCellRenderer.getInstance())
                .setTargets(resolvers)
                .createLineMarkerInfo(element)
    }
}
