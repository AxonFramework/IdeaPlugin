package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier

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
