package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.asJava.toLightMethods
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.psi.KtNamedFunction

class KotlinHandlerMethodLineMarker : AbstractHandlerLineMarker() {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element.parent !is KtNamedFunction || element !is LeafPsiElement || element.elementType !is KtModifierKeywordToken) {
            return null
        }
        val method = element.parent.toLightMethods().getOrNull(0) ?: return null
        return createLineMarker(element, method)
    }
}
