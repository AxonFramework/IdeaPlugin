package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiMethod
import org.axonframework.intellij.ide.plugin.annotator.ContainingMethodCellRenderer
import org.axonframework.intellij.ide.plugin.search.HandlerSearcher
import org.axonframework.intellij.ide.plugin.search.PublisherSearcher

class JavaHandlerMethodLineMarker : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is PsiIdentifier || element.parent !is PsiMethod) {
            return null
        }

        val method = element.parent as PsiMethod

        val handlers = HandlerSearcher.findAllMessageHandlers(element.project)
        val handler = handlers.firstOrNull { it.method == method }
        if(handler != null) {
            val publishers = PublisherSearcher.findPublishersForType(element.project, handler.payload)
            return NavigationGutterIconBuilder.create(AxonIcons.AxonIconOut)
                    .setTooltipText("Navigate to message constructors")
                    .setTargets(publishers)
                    .setCellRenderer(ContainingMethodCellRenderer())
                    .createLineMarkerInfo(element)
        }
        return null
    }
}
