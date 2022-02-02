package org.axonframework.intellij.ide.plugin.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiType
import com.intellij.psi.search.GlobalSearchScope

class JavaClassLineMarker : AbstractClassLineMarker() {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is PsiIdentifier || element.parent !is PsiClass) {
            return null
        }

        val method = element.parent as PsiClass

        val qualifiedName = method.qualifiedName ?: return null
        val psiType = PsiType.getTypeByName(qualifiedName, element.project, GlobalSearchScope.projectScope(element.project))

        return createLineMarker(element, psiType)
    }
}
