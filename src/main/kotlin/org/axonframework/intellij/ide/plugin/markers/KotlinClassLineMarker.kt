package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.elementType
import org.axonframework.intellij.ide.plugin.resolving.MessageHandlerResolver
import org.axonframework.intellij.ide.plugin.resolving.MessagePublisherResolver
import org.axonframework.intellij.ide.plugin.search.comparePsiElementsBasedOnDisplayName
import org.jetbrains.kotlin.idea.search.getKotlinFqName
import org.jetbrains.kotlin.lexer.KtKeywordToken
import org.jetbrains.kotlin.psi.KtClass

class KotlinClassLineMarker : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element.elementType !is KtKeywordToken || element.parent !is KtClass) {
            return null
        }

        val method = element.parent as KtClass

        val psiType = PsiType.getTypeByName(method.getKotlinFqName()!!.toString(), element.project, GlobalSearchScope.projectScope(element.project))
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
