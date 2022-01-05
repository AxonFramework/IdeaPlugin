package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.axonframework.intellij.ide.plugin.annotator.ContainingMethodCellRenderer
import org.axonframework.intellij.ide.plugin.search.HandlerSearcher
import org.axonframework.intellij.ide.plugin.search.PublisherSearcher
import org.jetbrains.kotlin.asJava.toLightMethods
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.psi.KtNamedFunction

class KotlinHandlerMethodLineMarker : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element.parent !is KtNamedFunction || element !is LeafPsiElement ||  element.elementType !is KtModifierKeywordToken) {
            return null
        }

        val method = element.parent
        val handlers = HandlerSearcher.findAllMessageHandlers(element.project)
        val handler = handlers.firstOrNull { it.method == method.toLightMethods()[0] }
        if(handler != null) {
            val publishers = PublisherSearcher.findPublishersForType(element.project, handler.payload)
            return NavigationGutterIconBuilder.create(AxonIcons.AxonIconOut)
                    .setTooltipText("Navigate to message constructors")
                    .setTargets(publishers)
//                    .setCellRenderer(ContainingMethodCellRenderer())
                    .createLineMarkerInfo(element)
        }
        return null
    }
}
