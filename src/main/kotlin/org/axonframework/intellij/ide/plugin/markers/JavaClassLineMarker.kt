package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiType
import com.intellij.psi.search.GlobalSearchScope
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.axonframework.intellij.ide.plugin.resolving.MessagePublisherResolver
import org.axonframework.intellij.ide.plugin.search.comparePsiElementsBasedOnDisplayName

class JavaClassLineMarker : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is PsiIdentifier || element.parent !is PsiClass) {
            return null
        }

        val method = element.parent as PsiClass

        val psiType = PsiType.getTypeByName(method.qualifiedName!!.toString(), element.project, GlobalSearchScope.projectScope(element.project))
        val handlers = element.project.getService(MessageHandlerResolver::class.java).findHandlersForType(psiType)
        val publishers = element.project.getService(MessagePublisherResolver::class.java).getPublishersForType(psiType)
        if (handlers.isEmpty() && publishers.isEmpty()) {
            return null
        }
        val items = (handlers.map { it.element } + publishers).sortedWith { a, b -> comparePsiElementsBasedOnDisplayName(element.project, a, b) }
        return NavigationGutterIconBuilder.create(AxonIcons.Both)
                .setTooltipText("Navigate to message references")
                .setCellRenderer(AxonCellRenderer.getInstance())
                .setTargets(items)
                .createLineMarkerInfo(element)
    }
}
