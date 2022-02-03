package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.jetbrains.kotlin.idea.search.getKotlinFqName
import org.jetbrains.kotlin.lexer.KtKeywordToken
import org.jetbrains.kotlin.psi.KtClass

class KotlinClassLineMarker : AbstractClassLineMarker() {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element.elementType !is KtKeywordToken || element.parent !is KtClass) {
            return null
        }

        val method = element.parent as KtClass

        val qualifiedName = method.getKotlinFqName()?.toString() ?: return null
        return createLineMarker(element, qualifiedName)
    }
}
