package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.idea.debugger.sequence.psi.resolveType
import org.jetbrains.kotlin.idea.refactoring.fqName.fqName
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

class KotlinPublishMethodLineMarker : AbstractPublisherLineMarker() {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element.parent !is KtNameReferenceExpression || element.parent.parent !is KtCallExpression) {
            return null
        }
        val resolvedType = (element.parent.parent as KtCallExpression?)?.resolveType() ?: return null
        val fq = resolvedType.fqName?.toString() ?: return null

        return createLineMarker(element, fq)
    }
}