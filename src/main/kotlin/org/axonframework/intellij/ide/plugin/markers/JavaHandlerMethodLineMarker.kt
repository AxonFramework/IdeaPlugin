package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.axonframework.intellij.ide.plugin.resolving.MessagePublisherResolver

class JavaHandlerMethodLineMarker : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is PsiIdentifier || element.parent !is PsiMethod) {
            return null
        }

        val method = element.parent as PsiMethod

        val handler = element.project.getService(MessageHandlerResolver::class.java)
                .findHandlerByElement(method) ?: return null

        val publisherResolver = element.project.getService(MessagePublisherResolver::class.java)
        val publishers = publisherResolver.getPublishersForType(handler.payloadType)
        return NavigationGutterIconBuilder.create(AxonIcons.Handler)
                .setTooltipText("Navigate to ${handler.messageType.displayName()} constructor")
                .setCellRenderer(AxonCellRenderer.getInstance())
                .setTargets(publishers)
                .createLineMarkerInfo(element)
    }
}
