package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier

/**
 * Renders a line marker icon when a class has a handler defined in the application.
 * The list contains both creators and handlers.
 */
class JavaClassLineMarker : AbstractClassLineMarker() {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is PsiIdentifier || element.parent !is PsiClass) {
            return null
        }

        val clazz = element.parent as PsiClass
        val qualifiedName = clazz.qualifiedName ?: return null

        return createLineMarker(element, qualifiedName)
    }
}
