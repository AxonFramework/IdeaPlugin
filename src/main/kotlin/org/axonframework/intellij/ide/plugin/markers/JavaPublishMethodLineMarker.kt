package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiNewExpression

/**
 * Renders a line marker when an instance of a class is created.
 * Contains all handlers for that payload type.
 */
class JavaPublishMethodLineMarker : AbstractPublisherLineMarker() {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is PsiIdentifier || element.parent.parent !is PsiNewExpression) {
            return null
        }
        val parent = element.parent.parent as PsiNewExpression
        val type = parent.classReference?.qualifiedName ?: return null
        return createLineMarker(element, type)
    }
}
