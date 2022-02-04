package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.asJava.toLightMethods
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Shows a line marker at a method defition if it is also a handler.
 * Navigates to creators of the payload.
 */
class KotlinHandlerMethodLineMarker : AbstractHandlerLineMarker() {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element.parent !is KtNamedFunction || element !is LeafPsiElement || element.elementType !is KtModifierKeywordToken) {
            return null
        }
        val method = element.parent.toLightMethods().getOrNull<PsiMethod>(0) ?: return null
        return createLineMarker(element, method)
    }
}
