package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiNewExpression
import org.axonframework.intellij.ide.plugin.search.HandlerSearcher

class JavaPublishMethodLineMarker : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is PsiIdentifier || element.parent.parent !is PsiNewExpression) {
            return null
        }
        val parent = element.parent.parent as PsiNewExpression
        val handlers = HandlerSearcher.findAllMessageHandlers(element.project).filter { it.payload.isAssignableFrom(parent.type!!) }
        if (handlers.isEmpty()) {
            return null
        }
        return NavigationGutterIconBuilder.create(AxonIcons.AxonIconOut)
                .setTooltipText("Navigate to Axon handlers")
                .setTargets(handlers.map { it.method })
                .createLineMarkerInfo(element)
    }
}
