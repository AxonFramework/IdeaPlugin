package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiType
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.axonframework.intellij.ide.plugin.resolving.MessagePublisherResolver
import org.axonframework.intellij.ide.plugin.search.comparePsiElementsBasedOnDisplayName

abstract class AbstractClassLineMarker : LineMarkerProvider {
    protected fun createLineMarker(element: PsiElement, psiType: PsiType): RelatedItemLineMarkerInfo<PsiElement>? {
        val handlers = element.project.getService(MessageHandlerResolver::class.java).findHandlersForType(psiType)
        if (handlers.isEmpty()) {
            return null
        }
        val publishers = element.project.getService(MessagePublisherResolver::class.java).getConstructorPublishersForType(psiType)

        val items = (handlers.map { it.element } + publishers).sortedWith { a, b -> comparePsiElementsBasedOnDisplayName(element.project, a, b) }
        return NavigationGutterIconBuilder.create(AxonIcons.Both)
                .setTooltipText("Navigate to message references")
                .setCellRenderer(AxonCellRenderer.getInstance())
                .setTargets(items)
                .createLineMarkerInfo(element)

    }
}
